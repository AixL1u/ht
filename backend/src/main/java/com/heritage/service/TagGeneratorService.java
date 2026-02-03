package com.heritage.service;

import org.springframework.stereotype.Service;
import java.util.*;
import java.util.stream.Collectors;

@Service
public class TagGeneratorService {
    
    // 预定义的标签库
    private static final Map<String, List<String>> TAG_KEYWORDS = new HashMap<>();
    
    static {
        // 初始化标签关键词映射
        TAG_KEYWORDS.put("京剧", Arrays.asList("京剧", "脸谱", "唱腔", "表演", "传统戏曲"));
        TAG_KEYWORDS.put("书法", Arrays.asList("书法", "笔画", "字体", "篆书", "楷书"));
        TAG_KEYWORDS.put("绘画", Arrays.asList("绘画", "国画", "水墨", "山水", "花鸟"));
        TAG_KEYWORDS.put("手工艺", Arrays.asList("手工", "工艺", "编织", "陶艺", "刺绣"));
        TAG_KEYWORDS.put("民间艺术", Arrays.asList("民间", "艺术", "民俗", "传统", "文化"));
        TAG_KEYWORDS.put("音乐", Arrays.asList("音乐", "乐器", "民乐", "琴", "笛"));
        TAG_KEYWORDS.put("舞蹈", Arrays.asList("舞蹈", "舞蹈艺术", "民族舞", "表演"));
        TAG_KEYWORDS.put("美食", Arrays.asList("美食", "烹饪", "菜肴", "食文化", "传统食品"));
    }
    
    /**
     * 生成标签
     */
    public List<String> generateTags(String content) {
        if (content == null || content.isEmpty()) {
            return new ArrayList<>();
        }
        
        Set<String> tags = new LinkedHashSet<>();
        String lowerContent = content.toLowerCase();
        
        // 根据关键词匹配生成标签
        for (Map.Entry<String, List<String>> entry : TAG_KEYWORDS.entrySet()) {
            String tag = entry.getKey();
            List<String> keywords = entry.getValue();
            
            for (String keyword : keywords) {
                if (lowerContent.contains(keyword.toLowerCase())) {
                    tags.add(tag);
                    break;
                }
            }
        }
        
        // 如果没有匹配到标签，进行分词处理
        if (tags.isEmpty()) {
            tags.addAll(extractKeywords(content));
        }
        
        // 限制标签数量为 5 个
        return tags.stream()
            .limit(5)
            .collect(Collectors.toList());
    }
    
    /**
     * 提取关键词（简单的分词实现）
     */
    public List<String> extractKeywords(String content) {
        if (content == null || content.isEmpty()) {
            return new ArrayList<>();
        }
        
        // 分割内容为单词
        String[] words = content.split("[\\s\\p{P}]+");
        
        // 过滤短词和停用词
        Set<String> stopWords = getStopWords();
        
        return Arrays.stream(words)
            .filter(word -> word.length() > 1 && !stopWords.contains(word.toLowerCase()))
            .distinct()
            .limit(5)
            .collect(Collectors.toList());
    }
    
    /**
     * 获取标签建议
     */
    public List<String> getTagSuggestions(String prefix) {
        if (prefix == null || prefix.isEmpty()) {
            return new ArrayList<>(TAG_KEYWORDS.keySet());
        }
        
        String lowerPrefix = prefix.toLowerCase();
        return TAG_KEYWORDS.keySet().stream()
            .filter(tag -> tag.toLowerCase().startsWith(lowerPrefix))
            .collect(Collectors.toList());
    }
    
    /**
     * 获取所有可用标签
     */
    public List<String> getAllTags() {
        return new ArrayList<>(TAG_KEYWORDS.keySet());
    }
    
    /**
     * 获取停用词列表
     */
    private Set<String> getStopWords() {
        return new HashSet<>(Arrays.asList(
            "的", "一", "是", "在", "不", "了", "有", "和", "人", "这",
            "中", "大", "为", "上", "个", "国", "我", "以", "要", "他",
            "时", "来", "用", "们", "生", "到", "作", "地", "于", "出",
            "就", "分", "对", "成", "会", "可", "主", "发", "年", "动",
            "同", "工", "也", "能", "下", "过", "民", "前", "面", "书",
            "the", "a", "an", "and", "or", "but", "in", "on", "at", "to"
        ));
    }
}
