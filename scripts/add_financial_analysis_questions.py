import json
import os

# Configuration
QUESTIONS_FILE = "core/database/src/main/assets/questions.json"
CATEGORY = "7. Financial Analysis / 财报分析"

new_questions_data = [
    {
        "content": "Which SEC filing is an annual report that provides a comprehensive summary of a company's financial performance?\n哪份SEC文件是年度报告，提供了公司财务业绩的全面总结？",
        "options": [
            "Form 10-Q\n表格 10-Q",
            "Form 8-K\n表格 8-K",
            "Form 10-K\n表格 10-K",
            "Form S-1\n表格 S-1"
        ],
        "correctAnswerIndex": 2,
        "explanation": "Form 10-K is an annual report required by the SEC, giving a comprehensive summary of a company's financial performance. Form 10-Q is quarterly.\n表格 10-K 是SEC要求的年度报告，提供公司财务业绩的全面总结。表格 10-Q 是季度报告。",
        "category": CATEGORY
    },
    {
        "content": "Which SEC filing is required to be filed quarterly?\n哪份SEC文件要求每季度提交一次？",
        "options": [
            "Form 10-K\n表格 10-K",
            "Form 10-Q\n表格 10-Q",
            "Form 8-K\n表格 8-K",
            "Form 13-F\n表格 13-F"
        ],
        "correctAnswerIndex": 1,
        "explanation": "Form 10-Q is a comprehensive report of financial performance that must be submitted quarterly by all public companies.\n表格 10-Q 是所有上市公司必须每季度提交的财务业绩综合报告。",
        "category": CATEGORY
    },
    {
        "content": "Which form must a company file to announce major events that shareholders should know about?\n公司必须提交哪种表格来宣布股东应该知道的重大事件？",
        "options": [
            "Form 10-K\n表格 10-K",
            "Form 8-K\n表格 8-K",
            "Form 10-Q\n表格 10-Q",
            "Form 4\n表格 4"
        ],
        "correctAnswerIndex": 1,
        "explanation": "Form 8-K is the 'current report' companies must file with the SEC to announce major events that shareholders should know about.\n表格 8-K 是公司必须向SEC提交的“当前报告”，用于宣布股东应该知道的重大事件。",
        "category": CATEGORY
    },
    {
        "content": "Which of the following is excluded from the Quick Ratio calculation?\n以下哪项不包括在速动比率计算中？",
        "options": [
            "Cash\n现金",
            "Accounts Receivable\n应收账款",
            "Inventory\n库存",
            "Marketable Securities\n有价证券"
        ],
        "correctAnswerIndex": 2,
        "explanation": "The Quick Ratio measures liquidity by excluding inventory, which is considered less liquid than cash or receivables. Formula: (Current Assets - Inventory) / Current Liabilities.\n速动比率通过排除库存来衡量流动性，库存被认为比现金或应收账款流动性更差。公式：（流动资产 - 库存）/ 流动负债。",
        "category": CATEGORY
    },
    {
        "content": "What does Diluted EPS take into account that Basic EPS does not?\n稀释每股收益（Diluted EPS）考虑了基本每股收益（Basic EPS）没有考虑的什么因素？",
        "options": [
            "Preferred Dividends\n优先股股息",
            "Convertible Securities\n可转换证券",
            "Interest Expense\n利息支出",
            "Tax Rates\n税率"
        ],
        "correctAnswerIndex": 1,
        "explanation": "Diluted EPS calculates earnings per share assuming all convertible securities (like options, warrants, convertible bonds) are exercised.\n稀释每股收益计算假设所有可转换证券（如期权、认股权证、可转换债券）都被行权后的每股收益。",
        "category": CATEGORY
    },
    {
        "content": "A high P/E ratio usually implies that investors expect:\n高市盈率（P/E）通常意味着投资者预期：",
        "options": [
            "Lower future growth\n未来增长较低",
            "Higher future growth\n未来增长较高",
            "Bankruptcy\n破产",
            "Stable dividends only\n仅有稳定的股息"
        ],
        "correctAnswerIndex": 1,
        "explanation": "A high P/E ratio suggests that investors are willing to pay a higher price today because of growth expectations in the future.\n高市盈率表明，由于对未来的增长预期，投资者愿意在今天支付更高的价格。",
        "category": CATEGORY
    },
    {
        "content": "How is Dividend Yield calculated?\n股息率（Dividend Yield）是如何计算的？",
        "options": [
            "Annual Dividend / Earnings Per Share\n年度股息 / 每股收益",
            "Annual Dividend / Current Stock Price\n年度股息 / 当前股价",
            "Net Income / Dividend Paid\n净利润 / 支付的股息",
            "Stock Price / Annual Dividend\n股价 / 年度股息"
        ],
        "correctAnswerIndex": 1,
        "explanation": "Dividend Yield is the financial ratio that shows how much a company pays out in dividends each year relative to its stock price.\n股息率是显示公司每年支付的股息相对于其股价的财务比率。",
        "category": CATEGORY
    },
    {
        "content": "What is the formula for Working Capital?\n营运资金的公式是什么？",
        "options": [
            "Total Assets - Total Liabilities\n总资产 - 总负债",
            "Current Assets - Current Liabilities\n流动资产 - 流动负债",
            "Cash + Inventory\n现金 + 库存",
            "Net Income + Depreciation\n净利润 + 折旧"
        ],
        "correctAnswerIndex": 1,
        "explanation": "Working Capital is the difference between a company's current assets and current liabilities, measuring short-term financial health.\n营运资金是公司流动资产与流动负债之间的差额，衡量短期财务健康状况。",
        "category": CATEGORY
    },
    {
        "content": "When is Goodwill recorded on a balance sheet?\n商誉何时记录在资产负债表上？",
        "options": [
            "When a company generates high profits\n当公司产生高额利润时",
            "When a company acquires another company for more than the fair value of its net assets\n当一家公司以高于其净资产公允价值的价格收购另一家公司时",
            "Annually based on brand value estimation\n每年根据品牌价值估算",
            "When a patent is granted\n当专利获得批准时"
        ],
        "correctAnswerIndex": 1,
        "explanation": "Goodwill is an intangible asset that arises when a buyer acquires an existing business for more than the fair value of its identifiable net assets.\n商誉是一种无形资产，当买方以高于其可识别净资产公允价值的价格收购现有业务时产生。",
        "category": CATEGORY
    },
    {
        "content": "Which of the following is an example of a Cash Flow from Investing Activities?\n以下哪项是投资活动产生的现金流的例子？",
        "options": [
            "Paying dividends\n支付股息",
            "Issuing common stock\n发行普通股",
            "Purchase of property, plant, and equipment (PP&E)\n购买物业、厂房和设备 (PP&E)",
            "Receipt of customer payments\n收到客户付款"
        ],
        "correctAnswerIndex": 2,
        "explanation": "Investing activities involve the purchase and sale of long-term assets like PP&E. Dividends and stock issuance are financing; customer payments are operating.\n投资活动涉及购买和出售长期资产，如PP&E。股息和股票发行是融资活动；客户付款是经营活动。",
        "category": CATEGORY
    },
    {
        "content": "Which of the following is classified as a Cash Flow from Financing Activities?\n以下哪项被归类为筹资活动产生的现金流？",
        "options": [
            "Net Income\n净利润",
            "Depreciation\n折旧",
            "Repurchase of company stock (Treasury Stock)\n回购公司股票（库存股）",
            "Sale of old equipment\n出售旧设备"
        ],
        "correctAnswerIndex": 2,
        "explanation": "Financing activities include transactions involving debt, equity, and dividends. Buying back stock is a financing outflow.\n筹资活动包括涉及债务、股权和股息的交易。回购股票是筹资流出。",
        "category": CATEGORY
    },
    {
        "content": "Which type of auditor opinion is considered the 'cleanest' and most favorable?\n哪种类型的审计意见被认为是“最干净”和最有利的？",
        "options": [
            "Qualified Opinion\n保留意见",
            "Adverse Opinion\n否定意见",
            "Disclaimer of Opinion\n无法表示意见",
            "Unqualified Opinion\n无保留意见"
        ],
        "correctAnswerIndex": 3,
        "explanation": "An Unqualified Opinion states that the financial statements present fairly, in all material respects, the financial position of the entity.\n无保留意见表明财务报表在所有重大方面公允地反映了实体的财务状况。",
        "category": CATEGORY
    },
    {
        "content": "Under the Accrual Basis of accounting, when is revenue recognized?\n在权责发生制会计下，何时确认收入？",
        "options": [
            "When cash is received\n收到现金时",
            "When the performance obligation is satisfied (earned)\n当履约义务得到满足（赚取）时",
            "When the invoice is sent\n发送发票时",
            "At the end of the year\n年底"
        ],
        "correctAnswerIndex": 1,
        "explanation": "Accrual accounting recognizes revenue when it is earned and realizable, regardless of when cash changes hands.\n权责发生制会计在收入赚取并可实现时确认收入，无论现金何时易手。",
        "category": CATEGORY
    },
    {
        "content": "What is Depreciation in accounting?\n会计中的折旧是什么？",
        "options": [
            "The decrease in market value of an asset\n资产市场价值的下降",
            "The allocation of the cost of a tangible asset over its useful life\n有形资产成本在其使用寿命内的分摊",
            "A cash expense paid annually\n每年支付的现金支出",
            "The loss of inventory due to theft\n因盗窃造成的库存损失"
        ],
        "correctAnswerIndex": 1,
        "explanation": "Depreciation is the systematic allocation of the cost of a tangible asset over its useful life. It is a non-cash expense.\n折旧是有形资产成本在其使用寿命内的系统分摊。它是一种非现金支出。",
        "category": CATEGORY
    },
    {
        "content": "Which financial statement reports a company's assets, liabilities, and shareholders' equity at a specific point in time?\n哪份财务报表报告了公司在特定时间点的资产、负债和股东权益？",
        "options": [
            "Income Statement\n利润表",
            "Statement of Cash Flows\n现金流量表",
            "Balance Sheet\n资产负债表",
            "Statement of Retained Earnings\n留存收益表"
        ],
        "correctAnswerIndex": 2,
        "explanation": "The Balance Sheet is a snapshot of the company's financial position at a single point in time.\n资产负债表是公司在单一时间点财务状况的快照。",
        "category": CATEGORY
    },
    {
        "content": "Which financial statement shows the company's profitability over a period of time?\n哪份财务报表显示了公司在一段时间内的盈利能力？",
        "options": [
            "Balance Sheet\n资产负债表",
            "Income Statement\n利润表",
            "Statement of Cash Flows\n现金流量表",
            "Shareholder Equity Statement\n股东权益表"
        ],
        "correctAnswerIndex": 1,
        "explanation": "The Income Statement reports revenues, expenses, and net income over a specific period (e.g., a quarter or year).\n利润表报告特定时期（例如季度或年度）的收入、支出和净利润。",
        "category": CATEGORY
    },
    {
        "content": "Which section of the Cash Flow Statement starts with Net Income in the indirect method?\n在间接法中，现金流量表的哪个部分以净利润开始？",
        "options": [
            "Investing Activities\n投资活动",
            "Financing Activities\n筹资活动",
            "Operating Activities\n经营活动",
            "Supplemental Activities\n补充活动"
        ],
        "correctAnswerIndex": 2,
        "explanation": "The Operating Activities section in the indirect method starts with Net Income and adjusts for non-cash items and working capital changes.\n间接法中的经营活动部分以净利润开始，并针对非现金项目和营运资金变化进行调整。",
        "category": CATEGORY
    }
]

def main():
    if not os.path.exists(QUESTIONS_FILE):
        print(f"Error: File not found at {QUESTIONS_FILE}")
        return

    try:
        with open(QUESTIONS_FILE, 'r', encoding='utf-8') as f:
            questions = json.load(f)
    except json.JSONDecodeError as e:
        print(f"Error decoding JSON: {e}")
        return

    # Find the max ID
    max_id = 0
    if questions:
        max_id = max(q.get('id', 0) for q in questions)

    print(f"Current max ID: {max_id}")
    print(f"Adding {len(new_questions_data)} new questions...")

    # Add new questions
    for i, q_data in enumerate(new_questions_data):
        new_id = max_id + 1 + i
        new_question = {
            "id": new_id,
            "content": q_data["content"],
            "options": q_data["options"],
            "correctAnswerIndex": q_data["correctAnswerIndex"],
            "explanation": q_data["explanation"],
            "category": q_data["category"]
        }
        questions.append(new_question)

    # Save back to file
    with open(QUESTIONS_FILE, 'w', encoding='utf-8') as f:
        json.dump(questions, f, ensure_ascii=False, indent=2)

    print(f"Successfully added {len(new_questions_data)} questions.")
    print(f"New total count: {len(questions)}")
    
    # Count category items
    category_count = sum(1 for q in questions if q.get("category") == CATEGORY)
    print(f"Total questions in '{CATEGORY}': {category_count}")

if __name__ == "__main__":
    main()
