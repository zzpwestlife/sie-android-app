#!/usr/bin/env python3
import re

input_file = "core/database/src/androidTest/java/com/example/sie/core/database/dao/QuestionDaoTest.kt"

with open(input_file, 'r', encoding='utf-8') as f:
    content = f.read()

# 替换 QuestionEntity 的创建
# Pattern: content = "xxx" -> content_en = "xxx", content_zh = "xxx"
# options = "xxx" -> options_en = "xxx", options_zh = "xxx"
# explanation = "xxx" -> explanation_en = "xxx", explanation_zh = "xxx"
# category = "xxx" -> category_en = "xxx", category_zh = "xxx"

# 替换 content = "..." 为 content_en = "..." 和 content_zh = "..."
content = re.sub(
    r'content = "(.*?)",',
    r'content_en = "\1",\n            content_zh = "\1",',
    content
)

# 替换 options = "..." 为 options_en = "..." 和 options_zh = "..."
content = re.sub(
    r'options = "(.*?)",',
    r'options_en = "\1",\n            options_zh = "\1",',
    content
)

# 替换 explanation = "..." 为 explanation_en = "..." 和 explanation_zh = "..."
content = re.sub(
    r'explanation = "(.*?)",',
    r'explanation_en = "\1",\n            explanation_zh = "\1",',
    content
)

# 替换 category = "..." 为 category_en = "..." 和 category_zh = "..." 和 category_short = "..."
content = re.sub(
    r'category = "(.*?)",',
    r'category_en = "\1",\n            category_zh = "\1",\n            category_short = "\1",',
    content
)

with open(input_file, 'w', encoding='utf-8') as f:
    f.write(content)

print("Done!")
