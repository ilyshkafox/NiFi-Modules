package ru.ilyshkafox.nifi.vk.client.controllers.cookieencoder;

import lombok.*;
import org.apache.commons.codec.digest.DigestUtils;
import ru.ilyshkafox.nifi.vk.client.controllers.utils.Assert;
import ru.ilyshkafox.nifi.vk.client.controllers.utils.Base64Utils;

import javax.crypto.*;
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.SecretKeySpec;
import java.lang.reflect.Array;
import java.nio.charset.StandardCharsets;
import java.security.InvalidAlgorithmParameterException;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.util.Arrays;
import java.util.Random;

@RequiredArgsConstructor
public class AesCookieEncoder implements CookieEncoder {
    private final static Random r = new Random();
    @Getter
    @Setter(AccessLevel.PACKAGE)
    private String password;

    public AesCookieEncoder(String password) {
        this.password = password;
        Assert.notNull(password, "Не указан пароль для хранилища куки!");
    }

    @Override
    public String decode(String text) {
        var cryptArr = Base64Utils.decodeFromString(text);
        var salt = Arrays.copyOfRange(cryptArr, 8, 16);
        var pbe = openSSLKey(password.getBytes(StandardCharsets.ISO_8859_1), salt);
        var key = pbe.getSecretKeySpec();
        var iv = pbe.getIvParameterSpec();
        cryptArr = Arrays.copyOfRange(cryptArr, 16, cryptArr.length);
        return new String(decrypt(cryptArr, key, iv), StandardCharsets.ISO_8859_1);
    }

    @Override
    public String encode(String text) {
        var salt = randArr(8);
        var pbe = openSSLKey(password.getBytes(StandardCharsets.ISO_8859_1), salt);
        var key = pbe.getSecretKeySpec();
        var iv = pbe.getIvParameterSpec();
        var saltBlock = concatWithCopy(new byte[]{83, 97, 108, 116, 101, 100, 95, 95}, salt);
        var string = text.getBytes(StandardCharsets.ISO_8859_1);
        var cipherBlocks = encrypt(string, key, iv);
        cipherBlocks = concatWithCopy(saltBlock, cipherBlocks);
        return Base64Utils.encodeToString(cipherBlocks);
    }

    private byte[] decrypt(byte[] cryptArr, SecretKey key, IvParameterSpec iv) {
        try {
            Cipher decrypt = Cipher.getInstance("AES/CBC/PKCS5Padding");
            decrypt.init(Cipher.DECRYPT_MODE, key, iv);
            decrypt.doFinal(cryptArr);
            return decrypt.doFinal(cryptArr);
        } catch (NoSuchAlgorithmException | NoSuchPaddingException | InvalidAlgorithmParameterException | InvalidKeyException | IllegalBlockSizeException | BadPaddingException e) {
            throw new RuntimeException("При расшифровке произошла ошибка. " + e.getMessage(), e);
        }
    }

    private byte[] encrypt(byte[] cryptArr, SecretKey key, IvParameterSpec iv) {
        try {
            Cipher decrypt = Cipher.getInstance("AES/CBC/PKCS5Padding");
            decrypt.init(Cipher.ENCRYPT_MODE, key, iv);
            decrypt.doFinal(cryptArr);
            return decrypt.doFinal(cryptArr);
        } catch (NoSuchAlgorithmException | NoSuchPaddingException | InvalidAlgorithmParameterException | InvalidKeyException | IllegalBlockSizeException | BadPaddingException e) {
            throw new RuntimeException("При расшифровке произошла ошибка. " + e.getMessage(), e);
        }
    }

    private byte[] randArr(int size) {
        byte[] res = new byte[size];
        r.nextBytes(res);
        return res;
    }

    private Result openSSLKey(byte[] passwordArr, byte[] saltArr) {
        byte[][] md5_hash = {{}, {}, {}};
        byte[] result;
        byte[] data00 = concatWithCopy(passwordArr, saltArr);

        md5_hash[0] = DigestUtils.md5(data00);

        result = md5_hash[0];
        for (int i = 1; i < 3; i++) {
            md5_hash[i] = DigestUtils.md5(concatWithCopy(md5_hash[i - 1], data00));
            result = concatWithCopy(result, md5_hash[i]);
        }
        byte[] key = Arrays.copyOfRange(result, 0, 4 * 8);
        byte[] iv = Arrays.copyOfRange(result, 4 * 8, 4 * 8 + 16);

        return Result.of(new SecretKeySpec(key, "AES"), new IvParameterSpec(iv));
    }

    @SuppressWarnings({"unchecked", "SuspiciousSystemArraycopy"})
    private <T> T concatWithCopy(T array1, T array2) {
        if (!array1.getClass().isArray() || !array2.getClass().isArray()) {
            throw new IllegalArgumentException("Only arrays are accepted.");
        }

        Class<?> compType1 = array1.getClass().getComponentType();
        Class<?> compType2 = array2.getClass().getComponentType();

        if (!compType1.equals(compType2)) {
            throw new IllegalArgumentException("Two arrays have different types.");
        }

        int len1 = Array.getLength(array1);
        int len2 = Array.getLength(array2);

        //the cast is safe due to the previous checks
        T result = (T) Array.newInstance(compType1, len1 + len2);
        System.arraycopy(array1, 0, result, 0, len1);
        System.arraycopy(array2, 0, result, len1, len2);
        return result;
    }

    @Data
    @AllArgsConstructor(staticName = "of")
    private static class Result {
        private final SecretKeySpec secretKeySpec;
        private final IvParameterSpec ivParameterSpec;
    }


}
