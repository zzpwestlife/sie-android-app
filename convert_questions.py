#!/usr/bin/env python3
import json
import os

# 读取原始文件
input_file = "core/database/src/main/assets/questions.json"
output_file = "core/database/src/main/assets/questions_new.json"

with open(input_file, 'r', encoding='utf-8') as f:
    questions = json.load(f)

# 处理每个问题
processed_questions = []

for q in questions:
    # 处理 content 字段
    content_parts = q['content'].split('\n')
    content_en = content_parts[0]
    content_zh = content_parts[1] if len(content_parts) > 1 else ''
    
    # 处理 options 字段
    options_en = []
    options_zh = []
    for opt in q['options']:
        opt_parts = opt.split('\n')
        options_en.append(opt_parts[0])
        options_zh.append(opt_parts[1] if len(opt_parts) > 1 else '')
    
    # 处理 explanation 字段
    explanation_parts = q['explanation'].split('\n')
    explanation_en = explanation_parts[0]
    explanation_zh = explanation_parts[1] if len(explanation_parts) > 1 else ''
    
    # 处理 category 字段
    category_parts = q['category'].split(' / ')
    # 提取英文分类，去除数字前缀
    category_en_raw = category_parts[0]
    category_en = category_en_raw.split('. ')[1] if '. ' in category_en_raw else category_en_raw
    # 提取中文分类
    category_zh = category_parts[1] if len(category_parts) > 1 else ''
    # 短分类使用英文分类
    category_short = category_en
    
    # 创建新的问题对象
    new_question = {
        'id': q['id'],
        'content_en': content_en,
        'content_zh': content_zh,
        'options_en': options_en,
        'options_zh': options_zh,
        'correctAnswerIndex': q['correctAnswerIndex'],
        'explanation_en': explanation_en,
        'explanation_zh': explanation_zh,
        'category_en': category_en,
        'category_zh': category_zh,
        'category_short': category_short
    }
    
    processed_questions.append(new_question)

# 写入新文件
with open(output_file, 'w', encoding='utf-8') as f:
    json.dump(processed_questions, f, ensure_ascii=False, indent=2)

print(f"转换完成！生成了新文件: {output_file}")
print(f"处理了 {len(processed_questions)} 个问题")
