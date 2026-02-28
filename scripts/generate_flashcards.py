import re
import json
import os

INPUT_FILE_MD = "/Users/admin/openSource/sie-android-app/EntryTest/入门考试学习资料_中英对照版.md"
OUTPUT_FILE = "/Users/admin/openSource/sie-android-app/core/database/src/main/assets/card.json"

def parse_markdown_to_flashcards(file_path):
    with open(file_path, 'r', encoding='utf-8') as f:
        lines = f.readlines()

    flashcards = []
    card_id = 1
    current_category = "General"
    current_card = None

    def save_current_card():
        nonlocal current_card, card_id
        if current_card and current_card['front'] and current_card['back']:
            current_card['id'] = card_id
            current_card['back'] = "".join(current_card['back']).strip()
            
            if not current_card['back']:
                current_card = None
                return

            image_match = re.search(r'!\[.*?\]\((.*?)\)', current_card['back'])
            if image_match:
                current_card['image'] = image_match.group(1).strip()
                current_card['back'] = re.sub(r'!\[.*?\]\(.*?\)', '', current_card['back']).strip()
            else:
                current_card['image'] = None

            flashcards.append(current_card)
            card_id += 1
        current_card = None

    for i, line in enumerate(lines):
        h1_match = re.match(r'^#\s+(.*)', line)
        h2_match = re.match(r'^##\s+(.*)', line)
        h3_match = re.match(r'^###\s+(.*)', line)
        h4_match = re.match(r'^####\s+(.*)', line)

        if h1_match:
            save_current_card()
            current_category = h1_match.group(1).strip()
            continue
        
        card_front_match = h2_match or h3_match or h4_match
        
        if card_front_match:
            save_current_card()
            front_text = card_front_match.group(1).strip().replace('**', '')
            
            if "以下例题" in front_text:
                continue

            current_card = {
                'category': current_category,
                'front': front_text,
                'back': []
            }
            continue
        
        if current_card:
            if re.match(r'^#{1,4}\s+', line) or re.match(r'^\s*---+\s*$', line) or "| ---" in line:
                save_current_card()
            else:
                if re.match(r'^\s*\|', line) and ('Answer:' in line or '答案：' in line):
                    save_current_card()
                    continue
                current_card['back'].append(line)

    save_current_card()
    
    return flashcards

def main():
    print(f"Parsing Markdown for flashcards: {INPUT_FILE_MD}...")
    try:
        flashcards = parse_markdown_to_flashcards(INPUT_FILE_MD)
        print(f"Generated {len(flashcards)} flashcards.")
        
        os.makedirs(os.path.dirname(OUTPUT_FILE), exist_ok=True)
        with open(OUTPUT_FILE, 'w', encoding='utf-8') as f:
            json.dump(flashcards, f, indent=2, ensure_ascii=False)
        print(f"Flashcards saved to {OUTPUT_FILE}")

    except Exception as e:
        print(f"Error generating flashcards: {e}")

if __name__ == "__main__":
    main()
