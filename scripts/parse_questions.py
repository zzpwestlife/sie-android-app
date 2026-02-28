import re
import json
import os
# import bs4 # Need BeautifulSoup to parse HTML

# Since we might not have bs4 installed in the environment, we will use simple regex for now 
# as the JS object structure in the HTML is quite regular.
# If regex proves brittle, we might need another approach, but let's try regex first for the JS object.

INPUT_FILE_MD = "/Users/admin/openSource/sie-android-app/EntryTest/入门考试学习资料_中英对照版.md"
INPUT_FILE_HTML = "/Users/admin/openSource/sie-android-app/EntryTest/entry-test.html"
OUTPUT_FILE = "/Users/admin/openSource/sie-android-app/core/database/src/main/assets/questions.json"

def parse_markdown(file_path):
    # ... (Keep existing markdown parsing logic) ...
    # Reuse the previous robust logic
    with open(file_path, 'r', encoding='utf-8') as f:
        content = f.read()

    questions = []
    current_category = "General"
    
    lines = content.split('\n')
    i = 0
    question_id_counter = 1
    
    # State for list-based questions
    pending_list_question = None

    def flush_pending_list_question():
        nonlocal pending_list_question, question_id_counter
        if pending_list_question:
            if pending_list_question['content'] and any(pending_list_question['options'].values()) and pending_list_question['correctAnswerIndex'] is not None:
                final_options = []
                valid_labels = ['A', 'B', 'C', 'D']
                for label in valid_labels:
                    if pending_list_question['options'][label]:
                        final_options.append("\n".join(pending_list_question['options'][label]))
                
                if final_options:
                    q_obj = {
                        "id": question_id_counter,
                        "content": pending_list_question['content'],
                        "options": final_options,
                        "correctAnswerIndex": pending_list_question['correctAnswerIndex'],
                        "explanation": pending_list_question['explanation'],
                        "category": pending_list_question['category']
                    }
                    questions.append(q_obj)
                    question_id_counter += 1
            pending_list_question = None

    while i < len(lines):
        line = lines[i].strip()
        
        if line.startswith('# '):
            flush_pending_list_question()
            header_text = line.lstrip('# ').strip()
            current_category = header_text

        if line.startswith('|') and ('Answer:' in line or '答案：' in line):
            flush_pending_list_question()
            row_content = line.strip('|')
            parts = row_content.split('|')
            if len(parts) >= 2:
                col1 = parts[0].strip()
                col2 = parts[-1].strip()
                
                col1_text = col1.replace('<br />', '\n').replace('<br/>', '\n')
                col1_lines = col1_text.split('\n')
                
                question_text_lines = []
                options_dict = {'A': [], 'B': [], 'C': [], 'D': []}
                
                for l in col1_lines:
                    l = l.strip()
                    if not l: continue
                    match = re.match(r'^[\(\s]*([A-D])[\)\.]\s+(.*)', l)
                    if match:
                        opt_label = match.group(1)
                        opt_content = match.group(2)
                        if opt_label in options_dict:
                            options_dict[opt_label].append(opt_content)
                    else:
                        has_options = any(options_dict.values())
                        if not has_options:
                            clean_l = l.replace('**', '')
                            clean_l = re.sub(r'^\d+\.\s*', '', clean_l)
                            question_text_lines.append(clean_l)
                
                question_content = "\n".join(question_text_lines).strip()
                final_options = []
                valid_labels = ['A', 'B', 'C', 'D']
                for label in valid_labels:
                    if options_dict[label]:
                        final_options.append("\n".join(options_dict[label]))
                
                if not final_options:
                    i += 1
                    continue

                col2_text = col2.replace('<br />', '\n').replace('<br/>', '\n')
                answer_match = re.search(r'Answer:\s*([A-D])', col2_text, re.IGNORECASE)
                if not answer_match:
                     answer_match = re.search(r'答案：\s*([A-D])', col2_text, re.IGNORECASE)
                
                correct_answer_index = 0
                if answer_match:
                    char = answer_match.group(1).upper()
                    correct_answer_index = ord(char) - ord('A')
                
                col2_lines = col2_text.split('\n')
                explanation_lines = []
                for l in col2_lines:
                    l = l.strip()
                    if not l: continue
                    if 'Answer:' in l or '答案：' in l: continue
                    l = re.sub(r'\*\*Explanation:\*\*', '', l)
                    l = re.sub(r'\*\*解释：\*\*', '', l)
                    l = re.sub(r'^Explanation:', '', l)
                    l = re.sub(r'^解释：', '', l)
                    explanation_lines.append(l.strip())
                
                explanation = "\n".join(explanation_lines).strip()
                explanation = explanation.replace('**', '')

                q_obj = {
                    "id": question_id_counter,
                    "content": question_content,
                    "options": final_options,
                    "correctAnswerIndex": correct_answer_index,
                    "explanation": explanation,
                    "category": current_category
                }
                questions.append(q_obj)
                question_id_counter += 1
            i += 1
            continue

        list_q_match = re.match(r'^\*\*[Qq]?\d+\.\s*(.*)', line)
        if list_q_match:
            flush_pending_list_question()
            q_content = list_q_match.group(1).strip().replace('**', '')
            if q_content.endswith('**'): q_content = q_content[:-2]
            pending_list_question = {
                'content': q_content,
                'options': {'A': [], 'B': [], 'C': [], 'D': []},
                'correctAnswerIndex': None,
                'explanation': "",
                'category': current_category
            }
            i += 1
            continue
        
        if pending_list_question:
            opt_match = re.match(r'^\*\s+([A-D])[\.\)]\s+(.*)', line)
            if not opt_match:
                 opt_match = re.match(r'^\s*([A-D])[\.\)]\s+(.*)', line)
            if opt_match:
                label = opt_match.group(1)
                content = opt_match.group(2).strip()
                if label in pending_list_question['options']:
                    pending_list_question['options'][label].append(content)
                i += 1
                continue
            
            if line.startswith('>') and ('Answer:' in line or '答案：' in line):
                ans_match = re.search(r'Answer:\s*([A-D])', line, re.IGNORECASE)
                if not ans_match:
                    ans_match = re.search(r'答案：\s*([A-D])', line, re.IGNORECASE)
                if ans_match:
                    char = ans_match.group(1).upper()
                    pending_list_question['correctAnswerIndex'] = ord(char) - ord('A')
                i += 1
                continue
            
            if line.startswith('>') and ('Explanation' in line or '解释' in line):
                expl_line = line.lstrip('>').strip()
                expl_line = re.sub(r'\*\*Explanation\*\*[:：]?', '', expl_line)
                expl_line = re.sub(r'\*\*解释\*\*[:：]?', '', expl_line)
                expl_line = re.sub(r'^Explanation[:：]?', '', expl_line)
                expl_line = re.sub(r'^解释[:：]?', '', expl_line)
                if pending_list_question['explanation']:
                    pending_list_question['explanation'] += "\n" + expl_line.strip()
                else:
                    pending_list_question['explanation'] = expl_line.strip()
                i += 1
                continue
            
            if pending_list_question['explanation'] and line.startswith('>'):
                expl_cont = line.lstrip('>').strip()
                pending_list_question['explanation'] += "\n" + expl_cont
                i += 1
                continue

        i += 1
    flush_pending_list_question()
    return questions

def parse_html(file_path, start_id=1):
    with open(file_path, 'r', encoding='utf-8') as f:
        content = f.read()
    
    questions = []
    
    # Extract the JS array 'const questions = [...]'
    # Pattern: const questions = [ ... ];
    # We use regex to find the content inside [ ... ]
    
    match = re.search(r'const questions = \[(.*?)\];', content, re.DOTALL)
    if not match:
        print("Could not find 'const questions' array in HTML.")
        return []
    
    js_array_content = match.group(1)
    
    # The content is a list of JS objects like:
    # {ch:0,q:{zh:'...',en:'...'}, opts:[{zh:'...',en:'...'},...], ans:1,exp:{zh:'...',en:'...'}},
    
    # We'll split by "}," to separate objects, roughly.
    # A more robust way is to parse the JS syntax.
    # Since we can't use a JS engine, we will use regex to extract each object.
    
    # Regex to match { ... } blocks. Non-greedy.
    # Be careful about nested braces if any. The structure seems simple enough.
    
    # Strategy: iterate through the string and count braces to find objects.
    
    current_obj_str = ""
    brace_count = 0
    in_obj = False
    
    objs = []
    for char in js_array_content:
        if char == '{':
            if brace_count == 0:
                in_obj = True
            brace_count += 1
        
        if in_obj:
            current_obj_str += char
            
        if char == '}':
            brace_count -= 1
            if brace_count == 0:
                in_obj = False
                objs.append(current_obj_str)
                current_obj_str = ""
    
    # Helper to extract value from key in JS object string
    def get_val(text, key):
        # key:{zh:'...',en:'...'}
        # key:1
        # Match key followed by colon
        # Value can be number, string, or object
        pass # Too complex to implement generic parser here.
    
    # Let's try to eval? No, unsafe.
    # Let's use specific regex for the known structure.
    
    for obj_str in objs:
        try:
            # Extract q (question)
            # q:{zh:'...',en:'...'}
            q_match = re.search(r'q:\{zh:\'(.*?)\',en:\'(.*?)\'\}', obj_str, re.DOTALL)
            if not q_match:
                 # Try double quotes or mixed
                 q_match = re.search(r'q:\{zh:[\'"](.*?)[\'"],en:[\'"](.*?)[\'"]\}', obj_str, re.DOTALL)
            
            q_zh = q_match.group(1) if q_match else ""
            q_en = q_match.group(2) if q_match else ""
            
            # Combine content
            content = f"{q_en}\n{q_zh}"
            
            # Extract opts
            # opts:[{zh:'...',en:'...'},...]
            opts_content = re.search(r'opts:\[(.*?)\]', obj_str, re.DOTALL).group(1)
            # Split opts
            # {zh:'...',en:'...'}, {zh:'...',en:'...'}
            # Again, brace counting or simple split if no nested commas in text
            # Assuming no commas in text for now, or we use regex for objects
            opt_matches = re.findall(r'\{zh:[\'"](.*?)[\'"],en:[\'"](.*?)[\'"]\}', opts_content)
            
            options = []
            for opt_zh, opt_en in opt_matches:
                options.append(f"{opt_en}\n{opt_zh}")
            
            # Extract ans (index)
            ans_match = re.search(r'ans:(\d+)', obj_str)
            correct_index = int(ans_match.group(1)) if ans_match else 0
            
            # Extract exp
            exp_match = re.search(r'exp:\{zh:[\'"](.*?)[\'"],en:[\'"](.*?)[\'"]\}', obj_str, re.DOTALL)
            exp_zh = exp_match.group(1) if exp_match else ""
            exp_en = exp_match.group(2) if exp_match else ""
            explanation = f"{exp_en}\n{exp_zh}"
            
            # Extract ch (chapter) to map to category
            ch_match = re.search(r'ch:(\d+)', obj_str)
            ch_idx = int(ch_match.group(1)) if ch_match else 0
            
            # Mapping chapter index to name (from the HTML 'chapters' array or just Generic)
            # 0: Macroeconomics, 1: Stocks, 2: ETF, 3: Options, 4: Funds, 5: Indexes, 6: Financial Analysis, 7: Revenue, 8: Ethics
            categories = [
                "1. Macroeconomics / 宏观经济学",
                "2. Stocks / 股票",
                "3. ETF / 交易所交易基金",
                "4. Options / 期权",
                "5. Funds / 基金",
                "6. Indexes / 常见指数",
                "7. Financial Analysis / 财报分析",
                "8. Revenue & Industry / 营收与行业分析",
                "9. Financial Ethics / 金融道德"
            ]
            category = categories[ch_idx] if ch_idx < len(categories) else "General"

            # Escape escaped quotes back
            content = content.replace("\\'", "'")
            options = [o.replace("\\'", "'") for o in options]
            explanation = explanation.replace("\\'", "'")

            q_obj = {
                "id": start_id,
                "content": content,
                "options": options,
                "correctAnswerIndex": correct_index,
                "explanation": explanation,
                "category": category
            }
            questions.append(q_obj)
            start_id += 1
            
        except Exception as e:
            print(f"Skipping an object due to parse error: {e}")
            continue
            
    return questions

def main():
    all_questions = []
    current_id = 1
    
    # 1. Parse Markdown (Existing) - SKIPPED as per user request
    # print(f"Parsing Markdown: {INPUT_FILE_MD}...")
    # try:
    #     md_questions = parse_markdown(INPUT_FILE_MD)
    #     # Re-assign IDs to be continuous
    #     for q in md_questions:
    #         q['id'] = current_id
    #         current_id += 1
    #     all_questions.extend(md_questions)
    #     print(f"Parsed {len(md_questions)} questions from Markdown.")
    # except Exception as e:
    #     print(f"Error parsing Markdown: {e}")

    # 2. Parse HTML (New)
    print(f"Parsing HTML: {INPUT_FILE_HTML}...")
    try:
        html_questions = parse_html(INPUT_FILE_HTML, start_id=current_id)
        all_questions.extend(html_questions)
        print(f"Parsed {len(html_questions)} questions from HTML.")
    except Exception as e:
        print(f"Error parsing HTML: {e}")
        
    # Deduplication?
    # Simple deduplication based on content similarity could be useful, 
    # but for now we assume they are complementary or user wants all.
    # User said "Add to", so we keep both.
    
    # Save to JSON
    print(f"Total questions: {len(all_questions)}")
    os.makedirs(os.path.dirname(OUTPUT_FILE), exist_ok=True)
    with open(OUTPUT_FILE, 'w', encoding='utf-8') as f:
        json.dump(all_questions, f, indent=2, ensure_ascii=False)
    print(f"Questions saved to {OUTPUT_FILE}")

if __name__ == "__main__":
    main()
