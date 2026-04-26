package net.dmulloy2.protocol.wrappers.login.serverbound;

import com.comphenix.protocol.BukkitInitialization;
import com.comphenix.protocol.PacketType;
import com.comphenix.protocol.events.PacketContainer;
import net.minecraft.network.protocol.login.ServerboundKeyPacket;
import net.minecraft.util.CryptException;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import javax.crypto.KeyGenerator;
import javax.crypto.SecretKey;
import java.security.KeyPairGenerator;
import java.security.PublicKey;

import static org.junit.jupiter.api.Assertions.*;

class WrappedServerboundKeyPacketTest {

    private static SecretKey secretKey;
    private static PublicKey publicKey;

    @BeforeAll
    static void beforeAll() throws Exception {
        BukkitInitialization.initializeAll();

        KeyGenerator keyGen = KeyGenerator.getInstance("AES");
        keyGen.init(128);
        secretKey = keyGen.generateKey();

        KeyPairGenerator kpGen = KeyPairGenerator.getInstance("RSA");
        kpGen.initialize(1024);
        publicKey = kpGen.generateKeyPair().getPublic();
    }

    @Test
    void testAllArgsCreate() {
        byte[] challenge = { 1, 2, 3, 4 };

        WrappedServerboundKeyPacket w = new WrappedServerboundKeyPacket(secretKey, publicKey, challenge);

        assertEquals(PacketType.Login.Client.ENCRYPTION_BEGIN, w.getHandle().getType());
        assertNotNull(w.getKeyBytes());
        assertNotNull(w.getEncryptedChallenge());
    }

    @Test
    void testNoArgsCreate() {
        WrappedServerboundKeyPacket w = new WrappedServerboundKeyPacket();

        assertEquals(PacketType.Login.Client.ENCRYPTION_BEGIN, w.getHandle().getType());
    }

    @Test
    void testModifyExistingPacket() throws CryptException {
        ServerboundKeyPacket nmsPacket = new ServerboundKeyPacket(secretKey, publicKey, new byte[] { 1, 2, 3, 4 });
        WrappedServerboundKeyPacket wrapper = new WrappedServerboundKeyPacket(
                new PacketContainer(WrappedServerboundKeyPacket.TYPE, nmsPacket));

        // The NMS constructor RSA-encrypts both the secret key and challenge,
        // so we read back whatever ciphertext was stored rather than asserting exact bytes.
        assertNotNull(wrapper.getKeyBytes());
        assertNotNull(wrapper.getEncryptedChallenge());

        wrapper.setKeyBytes(new byte[] { 10, 20, 30 });
        wrapper.setEncryptedChallenge(new byte[] { 10, 20, 30 });

        assertArrayEquals(new byte[] { 10, 20, 30 }, wrapper.getKeyBytes());
        assertArrayEquals(new byte[] { 10, 20, 30 }, wrapper.getEncryptedChallenge());
    }

    @Test
    void testWrongPacketTypeThrows() {
        assertThrows(IllegalArgumentException.class,
                () -> new WrappedServerboundKeyPacket(
                        new PacketContainer(PacketType.Play.Server.CHAT)));
    }
}
