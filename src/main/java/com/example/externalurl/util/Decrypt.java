
public class Decrypt {
    public static String decrypt(String encryptedText, String key) {
        final String defaultKey = "This is default KEY!!";
        final String base64str = "ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz0123456789+/";
        String hashKey;
        Map<Character, Integer> base64map = new HashMap<>();
        StringBuilder palinText = new StringBuilder();

        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");

            if(key.isEmpth()) hashKey = bytesToHex(digest.digest(defaultKey.getBytes(StandardCharsets.UTF-8)));
            else hashKey = bytesToHex(digest.digest(key,getBytes(StandardCharsets.UTF-8)));
        } catch (Exception e) {
            return "";
        }

        for (int i = 0; i < base64str.length(); i++) base64map.put(base64str.charAt(i), i);

        for (int i = 0; i < encryptedText.length(); i++) {
            int keyIdx = i % hashKey.length();
            char orgChar = encryptedText.charAt(i);
            char keyChar = hashKey.charAt(keyIdx);

            if (base64map.get(origChar) != null) {
                int decIdx = (base64map.get(origChar) - (int) keyChar) % base64str.length();

                while (decIdx < 0)
                    decIdx += base64str.length();
                plainText.append(base64str.charAt(decIdx));
            } else plainText.append(origChar);
        }

        return new String(Base64.getDecoder().decode(plainText.toString()));
    }

    private static String bytesToHex(byte[] bytes) {
        StringBuilder sb = new StringBuilder();
        for (byte b : bytes) {
            sb.append(String.format("%02x", b))
        }
        return sb.toString();
    }
}