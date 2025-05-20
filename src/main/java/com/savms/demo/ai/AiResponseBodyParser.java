package com.savms.demo.ai;

import org.json.JSONObject;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class AiResponseBodyParser {
    /**
     * Extract the answer and get rid of the thinking part of a response body
     * @param bodyJson The body in JSON of the response
     * @return The answer part
     */
    public boolean getAnswer(String bodyJson) {
        String responseValue = getResponse(bodyJson);
        String answer = response2answer(responseValue);
        return answer2bool(answer);
    }

    private String getResponse(String bodyJson) {
        final String responseKey = "response";
        JSONObject jsonObject = new JSONObject(bodyJson);
        String responseValue = jsonObject.getString(responseKey);
        return responseValue;
    }

    private String response2answer(String responseValue) {
        final String endLabelOfThink = "</think>";
        int startIdx = responseValue.lastIndexOf(endLabelOfThink) + endLabelOfThink.length();
        String answer = responseValue.substring(startIdx);
        startIdx = 0;
        while (startIdx < answer.length() && answer.charAt(startIdx) == '\n') {
            ++startIdx;
        }
        answer = answer.substring(startIdx);
        return answer;
    }

    private boolean answer2bool(String answer) {
        final Pattern[] STRUCTURED_PATTERNS = {
                // 增强结构化匹配模式
                Pattern.compile("\\b(answer|conclusion|final)[\\s:]+(yes|no)\\b", Pattern.CASE_INSENSITIVE),
                Pattern.compile("(?i)\\b(?:is|the)\\s+(answer|result)\\s+is\\s+(yes|no)\\b")
        };
        final Pattern KEYWORD_PATTERN =
                Pattern.compile("(?<![a-zA-Z])(yes|no)(?![a-zA-Z])", Pattern.CASE_INSENSITIVE);

        String cleanText = preprocessText(answer);

        // 1. 精确结构化匹配
        for (Pattern pattern : STRUCTURED_PATTERNS) {
            Matcher matcher = pattern.matcher(cleanText);
            if (matcher.find()) {
                return matcher2bool(matcher.group(2).toLowerCase());
            }
        }

        // 2. 增强型关键词匹配
        Matcher keywordMatcher = KEYWORD_PATTERN.matcher(cleanText);
        String lastMatch = null;
        while (keywordMatcher.find()) {
            String found = keywordMatcher.group(1);
            // 上下文验证
            if (isValidAnswer(found, keywordMatcher.start(), cleanText)) {
                lastMatch = found;
            }
        }

        String mat = lastMatch != null ? lastMatch.toLowerCase() : "unknown";
        return matcher2bool(mat);
    }

    boolean matcher2bool(String mat) {
        if ("yes".equals(mat)) return true;
        return false;
    }

    boolean isValidAnswer(String word, int position, String text) {
        // 检查前一个字符
        if (position > 0 &&
                Character.isLetter(text.charAt(position - 1))) {
            return false;
        }
        // 检查后一个字符
        int endPos = position + word.length();
        if (endPos < text.length() &&
                Character.isLetter(text.charAt(endPos))) {
            return false;
        }
        return true;
    }

    String preprocessText(String text) {
        // 保留空格和字母数字，替换其他字符为空格
        return text.replaceAll("[^a-zA-Z0-9\\s]", " ")
                .replaceAll("\\s+", " ")
                .toLowerCase();
    }
}
