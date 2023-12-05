package common.sshd

import kotlinx.coroutines.runBlocking
import org.apache.sshd.client.ClientBuilder
import org.apache.sshd.client.SshClient
import org.apache.sshd.client.keyverifier.AcceptAllServerKeyVerifier
import org.apache.sshd.client.session.ClientSession
import org.apache.sshd.common.kex.BuiltinDHFactories
import org.apache.sshd.common.signature.BuiltinSignatures
import org.apache.sshd.putty.PuttyKeyUtils
import org.junit.jupiter.api.Test
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import java.io.ByteArrayOutputStream
import java.nio.charset.Charset
import java.nio.file.FileSystems
import java.security.KeyPair
import java.util.stream.Collectors


internal class SSH {

    private val client by lazy {
        initClient()
    }

    private val ppkPath by lazy {
        FileSystems.getDefault().getPath("C:\\Users\\Virogu\\.ssh", "S905D3KEY.ppk").also {
            logger.info("ppkPath: ${it.toFile().path}")
        }
    }

    private val keyPairs by lazy {
        mutableListOf<KeyPair>().apply {
            PuttyKeyUtils.DEFAULT_INSTANCE.loadKeyPairs(null, ppkPath, { _, resourceKey, retryIndex ->
                logger.info("get password, retryIndex: ${retryIndex}, resourceKey: $resourceKey")
                "_Ll168888"
            }).also {
                logger.info("putty key pairs: ${it.map { keyPair -> keyPair.public }}")
            }.also(::addAll)
        }
    }

    private fun initClient() = SshClient.setUpDefaultClient().apply {
        signatureFactories = BuiltinSignatures.VALUES.toList()
        keyExchangeFactories = BuiltinDHFactories.VALUES.stream()
            .map(ClientBuilder.DH2KEX)
            .collect(Collectors.toList())
        serverKeyVerifier = AcceptAllServerKeyVerifier.INSTANCE
    }

    @Test
    fun sshTest(): Unit = runBlocking {
        client.start()
        client.connect(
            "root",
            "192.168.5.66",
            "_Ll168888"
        ) { session ->
            logger.info("connected")
            session.exec("cat /system/build.prop").onSuccess {
                logger.info("success: $it")
            }.onFailure {
                logger.warn("fail. ", it)
            }
            logger.info("finished")
            session.close()
            client.close()
        }.onFailure {
            logger.warn("run fail. ", it)
        }
    }

    private fun SshClient.connect(
        user: String,
        host: String,
        password: String,
        port: Int = 22,
        timeout: Long = 30_000L,
        doOnConnected: (ClientSession) -> Unit = {}
    ) = runCatching {
        this.use { client ->
            val session = client.connect(user, host, port).also {
                it.await(timeout)
            }.session
            session.apply {
                addPasswordIdentity(password)
                keyPairs.forEach(::addPublicKeyIdentity)
            }.also {
                it.auth().verify(timeout)
            }
            session.use {
                doOnConnected(it)
            }
        }
    }

    private fun ClientSession.exec(
        vararg cmds: String,
        charset: Charset = Charsets.UTF_8
    ) = exec(cmds.toList(), charset)

    private fun ClientSession.exec(
        cmds: Collection<String>,
        charset: Charset = Charsets.UTF_8
    ): Result<String> {
        val stdout = ConsoleOutputStream()
        val stderr = ConsoleOutputStream()
        cmds.forEach {
            exec(it, stdout, stderr, charset)
        }
        return if (stderr.size() > 0) {
            val errorMessage = stderr.toString(Charsets.UTF_8)
            Result.failure(IllegalStateException("error: $errorMessage"))
        } else {
            Result.success(stdout.toString(Charsets.UTF_8))
        }
    }

    private fun ClientSession.exec(
        cmd: String,
        stdout: ByteArrayOutputStream = ConsoleOutputStream(),
        stderr: ByteArrayOutputStream = ConsoleOutputStream(),
        charset: Charset = Charsets.UTF_8
    ): Result<String> = try {
        executeRemoteCommand(cmd, stdout, stderr, charset)
        if (stderr.size() > 0) {
            val errorMessage = stderr.toString(Charsets.UTF_8)
            Result.failure(IllegalStateException("Error reported from remote command=$cmd, error: $errorMessage"))
        } else {
            Result.success(stdout.toString(Charsets.UTF_8))
        }
    } catch (e: Throwable) {
        if (stderr.size() > 0) {
            val errorMessage = stderr.toString(Charsets.UTF_8)
            Result.failure(IllegalStateException("Error reported from remote command=$cmd, error: $errorMessage"))
        } else {
            Result.failure(e)
        }
    }

    internal inner class ConsoleOutputStream : ByteArrayOutputStream() {
        override fun write(b: Int) {
            super.write(b)
            logger.debug(b.toChar().toString())
        }

        override fun write(b: ByteArray) {
            super.write(b)
            logger.debug(String(buf, Charsets.UTF_8))
        }

        override fun writeBytes(b: ByteArray?) {
            super.writeBytes(b)
            logger.debug(String(buf, Charsets.UTF_8))
        }

        override fun write(b: ByteArray, off: Int, len: Int) {
            super.write(b, off, len)
            logger.debug(String(buf, Charsets.UTF_8))
        }
    }

    companion object {
        private val logger: Logger = LoggerFactory.getLogger("Test")
    }

}