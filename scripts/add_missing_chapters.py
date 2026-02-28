import json
import os
import sys

# Define file paths
PROJECT_ROOT = os.path.dirname(os.path.dirname(os.path.abspath(__file__)))
JSON_PATH = os.path.join(PROJECT_ROOT, "core/database/src/main/assets/questions.json")

# Define new questions for Chapters 4-9
NEW_QUESTIONS = [
    # Chapter 4: Options / 期权 (10 questions)
    {
        "content": "What is the primary right of a call option buyer?\n看涨期权买方的主要权利是什么？",
        "options": [
            "Right to sell at strike price\n按行权价卖出的权利",
            "Right to buy at strike price\n按行权价买入的权利",
            "Obligation to buy at strike price\n按行权价买入的义务"
        ],
        "correctAnswerIndex": 1,
        "explanation": "A call option gives the buyer the right, but not the obligation, to buy the underlying asset at the strike price.\n看涨期权赋予买方以行权价购买标的资产的权利，但没有义务。",
        "category": "4. Options / 期权"
    },
    {
        "content": "What happens to the intrinsic value of a put option as the underlying price decreases?\n随着标的价格下跌，看跌期权的内在价值如何变化？",
        "options": [
            "Increases\n增加",
            "Decreases\n减少",
            "Remains constant\n保持不变"
        ],
        "correctAnswerIndex": 0,
        "explanation": "Put intrinsic value = Strike Price - Underlying Price. As underlying price falls, intrinsic value rises.\n看跌期权内在价值 = 行权价 - 标的价格。标的价格下跌时，内在价值上升。",
        "category": "4. Options / 期权"
    },
    {
        "content": "Which Greek measures option sensitivity to time decay?\n哪个希腊字母衡量期权对时间衰减的敏感度？",
        "options": [
            "Delta\nDelta",
            "Gamma\nGamma",
            "Theta\nTheta"
        ],
        "correctAnswerIndex": 2,
        "explanation": "Theta measures the rate of decline in the value of an option due to the passage of time.\nTheta 衡量期权价值随时间流逝而下降的速率。",
        "category": "4. Options / 期权"
    },
    {
        "content": "What is a covered call strategy?\n什么是备兑看涨期权策略？",
        "options": [
            "Buy stock + Buy put\n买入股票 + 买入看跌期权",
            "Buy stock + Sell call\n买入股票 + 卖出看涨期权",
            "Sell stock + Buy call\n卖出股票 + 买入看涨期权"
        ],
        "correctAnswerIndex": 1,
        "explanation": "A covered call involves holding a long position in an asset and writing (selling) call options on that same asset.\n备兑看涨期权指持有标的资产多头头寸并卖出该资产的看涨期权。",
        "category": "4. Options / 期权"
    },
    {
        "content": "When is a call option 'in the money' (ITM)?\n看涨期权何时处于“实值”（ITM）状态？",
        "options": [
            "Strike Price > Market Price\n行权价 > 市场价",
            "Strike Price < Market Price\n行权价 < 市场价",
            "Strike Price = Market Price\n行权价 = 市场价"
        ],
        "correctAnswerIndex": 1,
        "explanation": "A call option is ITM when the market price is higher than the strike price.\n当市场价格高于行权价时，看涨期权处于实值状态。",
        "category": "4. Options / 期权"
    },
    {
        "content": "Which option strategy limits both profit and loss?\n哪种期权策略既限制盈利又限制亏损？",
        "options": [
            "Long Straddle\n买入跨式组合",
            "Bull Call Spread\n牛市看涨价差",
            "Short Put\n卖出看跌期权"
        ],
        "correctAnswerIndex": 1,
        "explanation": "Spreads (like Bull Call Spread) have defined maximum profit and maximum loss.\n价差策略（如牛市看涨价差）具有确定的最大盈利和最大亏损。",
        "category": "4. Options / 期权"
    },
    {
        "content": "What is the maximum loss for a long call option buyer?\n买入看涨期权的最大亏损是多少？",
        "options": [
            "Unlimited\n无限",
            "Strike Price\n行权价",
            "Premium Paid\n支付的权利金"
        ],
        "correctAnswerIndex": 2,
        "explanation": "The maximum loss for an option buyer is limited to the premium paid.\n期权买方的最大亏损限于支付的权利金。",
        "category": "4. Options / 期权"
    },
    {
        "content": "Which factor does NOT affect option pricing in the Black-Scholes model?\nBlack-Scholes 模型中，哪个因素不影响期权定价？",
        "options": [
            "Volatility\n波动率",
            "Dividends (in basic model)\n股息（基础模型中）",
            "P/E Ratio\n市盈率"
        ],
        "correctAnswerIndex": 2,
        "explanation": "P/E Ratio is not an input in the Black-Scholes model. Inputs are price, strike, time, rate, and volatility.\n市盈率不是 Black-Scholes 模型的输入变量。输入包括价格、行权价、时间、利率和波动率。",
        "category": "4. Options / 期权"
    },
    {
        "content": "What is implied volatility?\n什么是隐含波动率？",
        "options": [
            "Historical price fluctuation\n历史价格波动",
            "Market's forecast of future volatility\n市场对未来波动的预测",
            "Risk-free rate volatility\n无风险利率波动"
        ],
        "correctAnswerIndex": 1,
        "explanation": "Implied volatility represents the market's view of the likelihood of changes in a given security's price.\n隐含波动率代表市场对特定证券价格变动可能性的看法。",
        "category": "4. Options / 期权"
    },
    {
        "content": "What is the break-even point for a long put?\n买入看跌期权的盈亏平衡点是多少？",
        "options": [
            "Strike Price + Premium\n行权价 + 权利金",
            "Strike Price - Premium\n行权价 - 权利金",
            "Premium - Strike Price\n权利金 - 行权价"
        ],
        "correctAnswerIndex": 1,
        "explanation": "For a long put, break-even is Strike Price minus Premium paid.\n对于买入看跌期权，盈亏平衡点是行权价减去支付的权利金。",
        "category": "4. Options / 期权"
    },

    # Chapter 5: Funds / 基金 (10 questions)
    {
        "content": "What is the main characteristic of an open-end mutual fund?\n开放式共同基金的主要特征是什么？",
        "options": [
            "Fixed number of shares\n固定份额数量",
            "Shares are issued and redeemed on demand\n按需发行和赎回份额",
            "Traded on exchanges like stocks\n像股票一样在交易所交易"
        ],
        "correctAnswerIndex": 1,
        "explanation": "Open-end funds issue and redeem shares daily at NAV based on investor demand.\n开放式基金根据投资者需求，每日按净值（NAV）发行和赎回份额。",
        "category": "5. Funds / 基金"
    },
    {
        "content": "How is the Net Asset Value (NAV) calculated?\n净资产价值（NAV）如何计算？",
        "options": [
            "(Assets - Liabilities) / Shares Outstanding\n(资产 - 负债) / 发行在外份额",
            "Assets / Shares Outstanding\n资产 / 发行在外份额",
            "(Assets + Income) / Shares Outstanding\n(资产 + 收入) / 发行在外份额"
        ],
        "correctAnswerIndex": 0,
        "explanation": "NAV = (Total Assets - Total Liabilities) / Number of Outstanding Shares.\nNAV = (总资产 - 总负债) / 发行在外的份额总数。",
        "category": "5. Funds / 基金"
    },
    {
        "content": "What is a load fund?\n什么是收费基金（Load Fund）？",
        "options": [
            "A fund with high management fees\n管理费高的基金",
            "A fund that charges a sales commission\n收取销售佣金的基金",
            "A fund that invests in debt\n投资于债务的基金"
        ],
        "correctAnswerIndex": 1,
        "explanation": "A load fund charges a commission or sales charge, either at purchase (front-end) or sale (back-end).\n收费基金在购买（前端）或出售（后端）时收取佣金或销售费用。",
        "category": "5. Funds / 基金"
    },
    {
        "content": "Which type of fund aims to replicate a market index?\n哪种类型的基金旨在复制市场指数？",
        "options": [
            "Active Fund\n主动型基金",
            "Index Fund\n指数基金",
            "Hedge Fund\n对冲基金"
        ],
        "correctAnswerIndex": 1,
        "explanation": "Index funds are passively managed to match the performance of a specific market index.\n指数基金是被动管理的，旨在匹配特定市场指数的表现。",
        "category": "5. Funds / 基金"
    },
    {
        "content": "What is a 12b-1 fee?\n什么是 12b-1 费用？",
        "options": [
            "Performance fee\n绩效费",
            "Marketing and distribution fee\n营销和分销费",
            "Early redemption fee\n提前赎回费"
        ],
        "correctAnswerIndex": 1,
        "explanation": "12b-1 fees are annual fees charged to cover marketing and distribution costs.\n12b-1 费用是每年收取的用于支付营销和分销成本的费用。",
        "category": "5. Funds / 基金"
    },
    {
        "content": "What distinguishes a closed-end fund from an open-end fund?\n封闭式基金与开放式基金的区别是什么？",
        "options": [
            "Fixed number of shares traded on exchange\n在交易所交易的固定份额",
            "Redeemable at NAV daily\n每日按 NAV 赎回",
            "No management fees\n无管理费"
        ],
        "correctAnswerIndex": 0,
        "explanation": "Closed-end funds have a fixed number of shares raised through an IPO and trade on exchanges.\n封闭式基金通过 IPO 筹集固定数量的份额，并在交易所进行交易。",
        "category": "5. Funds / 基金"
    },
    {
        "content": "Which document contains key information about a mutual fund's objectives and risks?\n哪份文件包含共同基金目标和风险的关键信息？",
        "options": [
            "Annual Report\n年度报告",
            "Prospectus\n招股说明书",
            "Statement of Additional Information\n补充信息声明"
        ],
        "correctAnswerIndex": 1,
        "explanation": "The prospectus provides essential details about investment objectives, risks, fees, and expenses.\n招股说明书提供有关投资目标、风险、费用和开支的基本细节。",
        "category": "5. Funds / 基金"
    },
    {
        "content": "What is a Money Market Fund?\n什么是货币市场基金？",
        "options": [
            "Invests in long-term bonds\n投资于长期债券",
            "Invests in short-term, low-risk debt\n投资于短期、低风险债务",
            "Invests in high-growth stocks\n投资于高增长股票"
        ],
        "correctAnswerIndex": 1,
        "explanation": "Money market funds invest in high-quality, short-term debt instruments and aim to maintain a stable value.\n货币市场基金投资于高质量的短期债务工具，旨在保持稳定的价值。",
        "category": "5. Funds / 基金"
    },
    {
        "content": "What is the 'expense ratio' of a fund?\n基金的“费用比率”是什么？",
        "options": [
            "Percentage of assets used for administrative expenses\n用于管理费用的资产百分比",
            "Ratio of profit to loss\n盈亏比率",
            "Dividend payout ratio\n股息支付率"
        ],
        "correctAnswerIndex": 0,
        "explanation": "The expense ratio measures the annual fund operating expenses as a percentage of its assets.\n费用比率衡量年度基金运营费用占其资产的百分比。",
        "category": "5. Funds / 基金"
    },
    {
        "content": "Are mutual fund dividends taxable?\n共同基金的股息需要纳税吗？",
        "options": [
            "No, never\n不，从不",
            "Yes, generally in the year received\n是的，通常在收到当年",
            "Only when shares are sold\n仅在出售份额时"
        ],
        "correctAnswerIndex": 1,
        "explanation": "Dividends and capital gains distributions are generally taxable in the year they are received, unless in a tax-advantaged account.\n除非在税收优惠账户中，否则股息和资本收益分配通常在收到当年应纳税。",
        "category": "5. Funds / 基金"
    },

    # Chapter 6: Indexes / 常见指数 (10 questions)
    {
        "content": "What is the S&P 500 index based on?\n标准普尔 500 指数基于什么？",
        "options": [
            "Price-weighted average of 500 stocks\n500 只股票的价格加权平均",
            "Market capitalization-weighted of 500 large US companies\n500 家美国大公司的市值加权",
            "Equal-weighted average of 500 stocks\n500 只股票的等权重平均"
        ],
        "correctAnswerIndex": 1,
        "explanation": "The S&P 500 is a market-cap-weighted index of 500 leading publicly traded companies in the U.S.\n标普 500 是美国 500 家主要上市公司的市值加权指数。",
        "category": "6. Indexes / 常见指数"
    },
    {
        "content": "Which index is price-weighted?\n哪个指数是价格加权的？",
        "options": [
            "S&P 500\n标普 500",
            "Dow Jones Industrial Average (DJIA)\n道琼斯工业平均指数",
            "Nasdaq Composite\n纳斯达克综合指数"
        ],
        "correctAnswerIndex": 1,
        "explanation": "The DJIA is a price-weighted index, meaning stocks with higher prices have more influence.\n道指是价格加权指数，意味着价格较高的股票具有更大的影响力。",
        "category": "6. Indexes / 常见指数"
    },
    {
        "content": "What does the Nasdaq Composite primarily represent?\n纳斯达克综合指数主要代表什么？",
        "options": [
            "Blue-chip industrial stocks\n蓝筹工业股",
            "Technology and growth stocks\n科技和成长股",
            "Small-cap stocks\n小盘股"
        ],
        "correctAnswerIndex": 1,
        "explanation": "The Nasdaq Composite is heavily weighted towards technology and growth companies.\n纳斯达克综合指数主要侧重于科技和成长型公司。",
        "category": "6. Indexes / 常见指数"
    },
    {
        "content": "What is the Russell 2000 index known for?\n罗素 2000 指数以什么闻名？",
        "options": [
            "Large-cap stocks\n大盘股",
            "Small-cap stocks\n小盘股",
            "International stocks\n国际股票"
        ],
        "correctAnswerIndex": 1,
        "explanation": "The Russell 2000 measures the performance of approximately 2,000 small-cap companies in the Russell 3000 Index.\n罗素 2000 衡量罗素 3000 指数中约 2,000 家小盘公司的表现。",
        "category": "6. Indexes / 常见指数"
    },
    {
        "content": "What is the VIX index?\n什么是 VIX 指数？",
        "options": [
            "Volatility Index based on S&P 500 options\n基于标普 500 期权的波动率指数",
            "Volume Index of NYSE\n纽交所成交量指数",
            "Value Investment Index\n价值投资指数"
        ],
        "correctAnswerIndex": 0,
        "explanation": "The VIX measures market expectations of near-term volatility conveyed by S&P 500 stock index option prices.\nVIX 衡量标普 500 股指期权价格所传达的市场对近期波动率的预期。",
        "category": "6. Indexes / 常见指数"
    },
    {
        "content": "Which index tracks stocks outside the US?\n哪个指数追踪美国以外的股票？",
        "options": [
            "Wilshire 5000\nWilshire 5000",
            "MSCI EAFE\nMSCI EAFE",
            "Russell 1000\n罗素 1000"
        ],
        "correctAnswerIndex": 1,
        "explanation": "MSCI EAFE tracks developed markets in Europe, Australasia, and the Far East.\nMSCI EAFE 追踪欧洲、大洋洲和远东的发达市场。",
        "category": "6. Indexes / 常见指数"
    },
    {
        "content": "What does 'float-adjusted' mean in index calculation?\n指数计算中的“流通调整”是什么意思？",
        "options": [
            "Adjusts for inflation\n针对通胀进行调整",
            "Counts only shares available to public investors\n仅计算公众投资者可获得的股份",
            "Adjusts for dividend payments\n针对股息支付进行调整"
        ],
        "correctAnswerIndex": 1,
        "explanation": "Float-adjusted capitalization counts only shares that are available to the public, excluding locked-in shares.\n流通调整市值仅计算公众可获得的股份，不包括锁定的股份。",
        "category": "6. Indexes / 常见指数"
    },
    {
        "content": "What is a 'Total Return' index?\n什么是“总回报”指数？",
        "options": [
            "Tracks only price changes\n仅追踪价格变化",
            "Includes reinvestment of dividends and distributions\n包括股息和分配的再投资",
            "Subtracts inflation from returns\n从回报中扣除通胀"
        ],
        "correctAnswerIndex": 1,
        "explanation": "A total return index assumes all cash distributions (dividends) are reinvested back into the index.\n总回报指数假设所有现金分配（股息）都再投资回指数中。",
        "category": "6. Indexes / 常见指数"
    },
    {
        "content": "Which sector has the highest weight in the S&P 500 (historically recent)?\n哪个板块在标普 500 中权重最高（近期历史）？",
        "options": [
            "Energy\n能源",
            "Information Technology\n信息技术",
            "Utilities\n公用事业"
        ],
        "correctAnswerIndex": 1,
        "explanation": "In recent years, Information Technology has been the largest sector by weight in the S&P 500.\n近年来，信息技术一直是标普 500 中权重最大的板块。",
        "category": "6. Indexes / 常见指数"
    },
    {
        "content": "What happens when a stock is removed from an index?\n当股票被移出指数时会发生什么？",
        "options": [
            "Index funds must sell the stock\n指数基金必须出售该股票",
            "Stock price usually increases\n股价通常会上涨",
            "Company goes bankrupt\n公司破产"
        ],
        "correctAnswerIndex": 0,
        "explanation": "Index funds tracking that index are forced to sell the stock, often leading to a short-term price drop.\n追踪该指数的指数基金被迫出售该股票，通常导致短期价格下跌。",
        "category": "6. Indexes / 常见指数"
    },

    # Chapter 7: Financial Analysis / 财报分析 (10 questions)
    {
        "content": "What does the P/E ratio measure?\n市盈率（P/E）衡量什么？",
        "options": [
            "Price relative to Sales\n价格相对于销售额",
            "Price relative to Earnings Per Share (EPS)\n价格相对于每股收益（EPS）",
            "Price relative to Book Value\n价格相对于账面价值"
        ],
        "correctAnswerIndex": 1,
        "explanation": "P/E Ratio = Market Price per Share / Earnings Per Share. It values a company relative to its earnings.\n市盈率 = 每股市价 / 每股收益。它相对于收益对公司进行估值。",
        "category": "7. Financial Analysis / 财报分析"
    },
    {
        "content": "Which financial statement shows a company's financial position at a specific point in time?\n哪份财务报表显示公司在特定时间点的财务状况？",
        "options": [
            "Income Statement\n利润表",
            "Balance Sheet\n资产负债表",
            "Cash Flow Statement\n现金流量表"
        ],
        "correctAnswerIndex": 1,
        "explanation": "The Balance Sheet reports assets, liabilities, and equity at a specific date.\n资产负债表报告特定日期的资产、负债和权益。",
        "category": "7. Financial Analysis / 财报分析"
    },
    {
        "content": "What is EBITDA?\n什么是 EBITDA？",
        "options": [
            "Earnings Before Interest, Taxes, Depreciation, and Amortization\n息税折旧摊销前利润",
            "Earnings Before Income, Tax, and Assets\n收入、税收和资产前利润",
            "Equity Before Interest and Tax Deductions\n利息和税收扣除前权益"
        ],
        "correctAnswerIndex": 0,
        "explanation": "EBITDA is a measure of a company's overall financial performance and is used as an alternative to net income.\nEBITDA 是衡量公司整体财务表现的指标，用作净收入的替代指标。",
        "category": "7. Financial Analysis / 财报分析"
    },
    {
        "content": "What does a Current Ratio < 1 indicate?\n流动比率 < 1 表明什么？",
        "options": [
            "Good long-term solvency\n良好的长期偿债能力",
            "Potential liquidity problems\n潜在的流动性问题",
            "High profitability\n高盈利能力"
        ],
        "correctAnswerIndex": 1,
        "explanation": "Current Ratio = Current Assets / Current Liabilities. A ratio < 1 suggests the company may struggle to pay short-term debts.\n流动比率 = 流动资产 / 流动负债。比率 < 1 表明公司可能难以偿还短期债务。",
        "category": "7. Financial Analysis / 财报分析"
    },
    {
        "content": "What is Free Cash Flow (FCF)?\n什么是自由现金流（FCF）？",
        "options": [
            "Operating Cash Flow - Capital Expenditures\n经营现金流 - 资本支出",
            "Net Income + Depreciation\n净收入 + 折旧",
            "Revenue - Expenses\n收入 - 支出"
        ],
        "correctAnswerIndex": 0,
        "explanation": "FCF represents the cash a company generates after accounting for cash outflows to support operations and maintain its capital assets.\nFCF 代表公司在扣除支持运营和维护资本资产的现金流出后产生的现金。",
        "category": "7. Financial Analysis / 财报分析"
    },
    {
        "content": "Which ratio measures profitability relative to shareholder equity?\n哪个比率衡量相对于股东权益的盈利能力？",
        "options": [
            "ROA (Return on Assets)\nROA（资产回报率）",
            "ROE (Return on Equity)\nROE（净资产收益率）",
            "ROI (Return on Investment)\nROI（投资回报率）"
        ],
        "correctAnswerIndex": 1,
        "explanation": "ROE = Net Income / Shareholders' Equity. It measures how effectively management is using a company's assets to create profits.\nROE = 净收入 / 股东权益。它衡量管理层利用公司资产创造利润的效率。",
        "category": "7. Financial Analysis / 财报分析"
    },
    {
        "content": "What is the 'top line' of an income statement?\n利润表的“顶线”是什么？",
        "options": [
            "Net Income\n净收入",
            "Revenue / Sales\n收入/销售额",
            "Gross Profit\n毛利"
        ],
        "correctAnswerIndex": 1,
        "explanation": "The top line refers to a company's gross revenue or sales.\n顶线指的是公司的总收入或销售额。",
        "category": "7. Financial Analysis / 财报分析"
    },
    {
        "content": "What does a high Debt-to-Equity ratio imply?\n高债务权益比意味着什么？",
        "options": [
            "Low financial leverage\n低财务杠杆",
            "High financial leverage and risk\n高财务杠杆和风险",
            "High liquidity\n高流动性"
        ],
        "correctAnswerIndex": 1,
        "explanation": "A high Debt-to-Equity ratio indicates that a company is primarily financing its growth with debt, implying higher risk.\n高债务权益比表明公司主要通过债务为其增长融资，意味着更高的风险。",
        "category": "7. Financial Analysis / 财报分析"
    },
    {
        "content": "In cash flow analysis, where do dividend payments appear?\n在现金流分析中，股息支付出现在哪里？",
        "options": [
            "Operating Activities\n经营活动",
            "Investing Activities\n投资活动",
            "Financing Activities\n筹资活动"
        ],
        "correctAnswerIndex": 2,
        "explanation": "Dividends paid to shareholders are reported under Cash Flow from Financing Activities.\n支付给股东的股息在筹资活动产生的现金流中报告。",
        "category": "7. Financial Analysis / 财报分析"
    },
    {
        "content": "What is 'Gross Margin'?\n什么是“毛利率”？",
        "options": [
            "(Revenue - COGS) / Revenue\n(收入 - 销货成本) / 收入",
            "Net Income / Revenue\n净收入 / 收入",
            "Operating Income / Revenue\n营业收入 / 收入"
        ],
        "correctAnswerIndex": 0,
        "explanation": "Gross Margin represents the percent of total sales revenue that the company retains after incurring the direct costs associated with producing the goods.\n毛利率代表公司在扣除生产商品的直接成本后保留的总销售收入百分比。",
        "category": "7. Financial Analysis / 财报分析"
    },

    # Chapter 8: Revenue & Industry / 行业分析 (10 questions)
    {
        "content": "Which stage of the industry life cycle is characterized by slowing growth and intense competition?\n行业生命周期的哪个阶段以增长放缓和竞争激烈为特征？",
        "options": [
            "Start-up\n初创期",
            "Growth\n成长期",
            "Shakeout / Maturity\n震荡/成熟期"
        ],
        "correctAnswerIndex": 2,
        "explanation": "In the shakeout/maturity phase, growth slows, weaker competitors exit, and competition becomes intense.\n在震荡/成熟阶段，增长放缓，较弱的竞争对手退出，竞争变得激烈。",
        "category": "8. Revenue & Industry / 行业分析"
    },
    {
        "content": "What is a cyclical industry?\n什么是周期性行业？",
        "options": [
            "Industry unaffected by economy\n不受经济影响的行业",
            "Industry sensitive to business cycles\n对商业周期敏感的行业",
            "Industry with constant growth\n持续增长的行业"
        ],
        "correctAnswerIndex": 1,
        "explanation": "Cyclical industries (e.g., autos, housing) perform well in expansions and poorly in recessions.\n周期性行业（如汽车、住房）在扩张期表现良好，在衰退期表现不佳。",
        "category": "8. Revenue & Industry / 行业分析"
    },
    {
        "content": "Which of these is a defensive sector?\n以下哪个是防御性板块？",
        "options": [
            "Consumer Discretionary\n非必需消费品",
            "Technology\n科技",
            "Consumer Staples\n必需消费品"
        ],
        "correctAnswerIndex": 2,
        "explanation": "Defensive sectors like Consumer Staples tend to remain stable regardless of the economic state.\n防御性板块（如必需消费品）往往无论经济状况如何都保持稳定。",
        "category": "8. Revenue & Industry / 行业分析"
    },
    {
        "content": "What is Porter's Five Forces model used for?\n波特五力模型用于什么？",
        "options": [
            "Analyzing macroeconomic trends\n分析宏观经济趋势",
            "Analyzing industry competitive intensity\n分析行业竞争强度",
            "Analyzing stock price patterns\n分析股价形态"
        ],
        "correctAnswerIndex": 1,
        "explanation": "Porter's Five Forces identifies and analyzes five competitive forces that shape every industry.\n波特五力模型识别并分析塑造每个行业的五种竞争力量。",
        "category": "8. Revenue & Industry / 行业分析"
    },
    {
        "content": "What is 'Same-Store Sales' (SSS) growth?\n什么是“同店销售”（SSS）增长？",
        "options": [
            "Total sales growth\n总销售增长",
            "Sales growth from stores open at least a year\n开业至少一年的店铺的销售增长",
            "Sales from new stores only\n仅来自新店的销售"
        ],
        "correctAnswerIndex": 1,
        "explanation": "SSS growth measures revenue growth for retail locations that have been open for a year or more, excluding new store impact.\nSSS 增长衡量开业一年或更长时间的零售地点的收入增长，排除新店影响。",
        "category": "8. Revenue & Industry / 行业分析"
    },
    {
        "content": "What is a high barrier to entry?\n什么是高进入壁垒？",
        "options": [
            "Easy for new competitors to enter\n新竞争者容易进入",
            "Difficult/Expensive for new competitors to enter\n新竞争者难以/昂贵进入",
            "Low regulatory requirements\n低监管要求"
        ],
        "correctAnswerIndex": 1,
        "explanation": "High barriers to entry (e.g., patents, high capital costs) protect existing companies from new competition.\n高进入壁垒（如专利、高资本成本）保护现有公司免受新竞争。",
        "category": "8. Revenue & Industry / 行业分析"
    },
    {
        "content": "How is market share calculated?\n市场份额如何计算？",
        "options": [
            "Company Revenue / Total Industry Revenue\n公司收入 / 行业总收入",
            "Company Profit / Total Industry Profit\n公司利润 / 行业总利润",
            "Company Assets / Total Industry Assets\n公司资产 / 行业总资产"
        ],
        "correctAnswerIndex": 0,
        "explanation": "Market share is the percentage of an industry's total sales that is earned by a particular company.\n市场份额是特定公司获得的行业总销售额的百分比。",
        "category": "8. Revenue & Industry / 行业分析"
    },
    {
        "content": "What characterizes a fragmented industry?\n分散行业的特征是什么？",
        "options": [
            "Dominated by one company\n由一家公司主导",
            "Dominated by a few large companies\n由几家大公司主导",
            "Many small competitors, none dominant\n许多小竞争者，无主导者"
        ],
        "correctAnswerIndex": 2,
        "explanation": "A fragmented industry has no single enterprise that has a large enough share of the market to influence the industry's direction.\n分散行业中没有一家企业拥有足够大的市场份额来影响行业方向。",
        "category": "8. Revenue & Industry / 行业分析"
    },
    {
        "content": "Which factor is LEAST likely to affect a utility company's revenue?\n哪个因素最不可能影响公用事业公司的收入？",
        "options": [
            "Regulatory rate changes\n监管费率变化",
            "Fashion trends\n时尚趋势",
            "Energy consumption patterns\n能源消费模式"
        ],
        "correctAnswerIndex": 1,
        "explanation": "Utilities are regulated and demand-driven; fashion trends have little to no impact.\n公用事业受监管且受需求驱动；时尚趋势几乎没有影响。",
        "category": "8. Revenue & Industry / 行业分析"
    },
    {
        "content": "What is 'Churn Rate'?\n什么是“流失率”？",
        "options": [
            "Rate of new customer acquisition\n新客户获取率",
            "Percentage of customers who stop using a service\n停止使用服务的客户百分比",
            "Employee turnover rate\n员工流动率"
        ],
        "correctAnswerIndex": 1,
        "explanation": "Churn rate is the rate at which customers stop doing business with an entity, crucial in subscription models.\n流失率是客户停止与实体做生意的比率，在订阅模式中至关重要。",
        "category": "8. Revenue & Industry / 行业分析"
    },

    # Chapter 9: Financial Ethics / 金融道德 (10 questions)
    {
        "content": "What is insider trading?\n什么是内幕交易？",
        "options": [
            "Trading based on public information\n基于公开信息交易",
            "Trading based on material non-public information\n基于重大非公开信息交易",
            "Trading by company executives legally\n公司高管合法交易"
        ],
        "correctAnswerIndex": 1,
        "explanation": "Insider trading involves trading a public company's stock by someone who has non-public, material information about that stock.\n内幕交易涉及拥有关于某上市公司股票的非公开重大信息的人进行交易。",
        "category": "9. Financial Ethics / 金融道德"
    },
    {
        "content": "What is a 'fiduciary duty'?\n什么是“信托责任”？",
        "options": [
            "Duty to maximize commissions\n最大化佣金的责任",
            "Legal obligation to act in the best interest of the client\n为客户最大利益行事的法律义务",
            "Duty to follow market trends\n跟随市场趋势的责任"
        ],
        "correctAnswerIndex": 1,
        "explanation": "Fiduciary duty requires a financial advisor to act solely in the best interest of their client.\n信托责任要求财务顾问完全为了客户的最大利益行事。",
        "category": "9. Financial Ethics / 金融道德"
    },
    {
        "content": "What is 'front-running'?\n什么是“抢先交易”？",
        "options": [
            "Broker trading for own account before client order\n经纪人在客户订单前为自己账户交易",
            "Running ahead of the market open\n在市场开盘前运行",
            "Prioritizing large clients\n优先考虑大客户"
        ],
        "correctAnswerIndex": 0,
        "explanation": "Front-running is the unethical practice of a broker trading an equity in their personal account based on advanced knowledge of pending orders from clients.\n抢先交易是经纪人基于对客户未决订单的预知，在个人账户中交易股票的不道德行为。",
        "category": "9. Financial Ethics / 金融道德"
    },
    {
        "content": "What is 'churning' in an account context?\n账户背景下的“过度交易”是什么？",
        "options": [
            "Excessive trading to generate commissions\n为了产生佣金而过度交易",
            "Mixing assets\n混合资产",
            "Moving money between accounts\n在账户间转移资金"
        ],
        "correctAnswerIndex": 0,
        "explanation": "Churning refers to a broker conducting excessive trading in a client's account mainly to generate commissions.\n过度交易是指经纪人在客户账户中进行过度交易，主要是为了产生佣金。",
        "category": "9. Financial Ethics / 金融道德"
    },
    {
        "content": "What constitutes a 'conflict of interest'?\n什么构成“利益冲突”？",
        "options": [
            "When interests of agent and principal align\n代理人和委托人利益一致时",
            "When personal interests compete with professional duties\n当个人利益与职业职责竞争时",
            "When two clients have the same portfolio\n当两个客户拥有相同的投资组合时"
        ],
        "correctAnswerIndex": 1,
        "explanation": "A conflict of interest occurs when an entity or individual becomes unreliable because of a clash between personal interests and professional duties.\n当个人利益与职业职责发生冲突导致实体或个人不可靠时，就会发生利益冲突。",
        "category": "9. Financial Ethics / 金融道德"
    },
    {
        "content": "What is 'Money Laundering'?\n什么是“洗钱”？",
        "options": [
            "Cleaning dirty banknotes\n清洗脏钞票",
            "Process of making illegally-gained proceeds appear legal\n使非法所得看起来合法的过程",
            "Donating money to charity\n向慈善机构捐款"
        ],
        "correctAnswerIndex": 1,
        "explanation": "Money laundering is the illegal process of concealing the origins of money obtained illegally.\n洗钱是隐瞒非法所得资金来源的非法过程。",
        "category": "9. Financial Ethics / 金融道德"
    },
    {
        "content": "What is the purpose of 'KYC' (Know Your Client)?\n'KYC'（了解你的客户）的目的是什么？",
        "options": [
            "To send birthday cards\n发送生日贺卡",
            "To verify identity and assess suitability/risk\n验证身份并评估适用性/风险",
            "To increase advertising reach\n增加广告覆盖面"
        ],
        "correctAnswerIndex": 1,
        "explanation": "KYC rules ensure investment advisors know detailed information about their clients' risk tolerance, investment knowledge, and financial position.\nKYC 规则确保投资顾问了解有关其客户风险承受能力、投资知识和财务状况的详细信息。",
        "category": "9. Financial Ethics / 金融道德"
    },
    {
        "content": "What is 'Market Manipulation'?\n什么是“市场操纵”？",
        "options": [
            "Natural price discovery\n自然价格发现",
            "Artificially inflating or deflating prices\n人为抬高或压低价格",
            "Hedging risk\n对冲风险"
        ],
        "correctAnswerIndex": 1,
        "explanation": "Market manipulation involves deliberate interference with the free and fair operation of the market.\n市场操纵涉及对市场自由公平运作的蓄意干预。",
        "category": "9. Financial Ethics / 金融道德"
    },
    {
        "content": "Can an analyst own stock in a company they cover?\n分析师可以持有他们所覆盖公司的股票吗？",
        "options": [
            "Yes, without disclosure\n可以，无需披露",
            "Yes, with full disclosure and restrictions\n可以，需全面披露并受限制",
            "No, never\n不，从不"
        ],
        "correctAnswerIndex": 1,
        "explanation": "Generally, analysts can own stock but must disclose it, and there are strict rules against trading contrary to their recommendations or ahead of reports.\n通常，分析师可以持股但必须披露，并且有严格规则禁止与推荐相反的交易或在报告前交易。",
        "category": "9. Financial Ethics / 金融道德"
    },
    {
        "content": "What is a 'Chinese Wall' (Information Barrier) in finance?\n金融中的“中国墙”（信息隔离墙）是什么？",
        "options": [
            "A physical wall in the office\n办公室里的实体墙",
            "Barrier to prevent exchange of non-public information between departments\n防止部门间非公开信息交换的障碍",
            "Trade barrier with China\n与中国的贸易壁垒"
        ],
        "correctAnswerIndex": 1,
        "explanation": "Information barriers separate departments (e.g., investment banking and research) to prevent conflicts of interest and insider trading.\n信息隔离墙分隔部门（例如投资银行和研究部），以防止利益冲突和内幕交易。",
        "category": "9. Financial Ethics / 金融道德"
    }
]

def main():
    print("Reading questions.json...")
    try:
        with open(JSON_PATH, 'r', encoding='utf-8') as f:
            questions = json.load(f)
    except FileNotFoundError:
        print(f"Error: File not found at {JSON_PATH}")
        return
    except json.JSONDecodeError:
        print("Error: Failed to decode JSON.")
        return

    print(f"Loaded {len(questions)} existing questions.")

    # Standardize existing categories
    changes_count = 0
    for q in questions:
        # Fix Chapter 3
        if q['category'].startswith("3. ETF"):
            if q['category'] != "3. ETFs / 交易所交易基金":
                q['category'] = "3. ETFs / 交易所交易基金"
                changes_count += 1
        
        # Fix Chapter 8
        if q['category'].startswith("8. Revenue"):
            if q['category'] != "8. Revenue & Industry / 行业分析":
                q['category'] = "8. Revenue & Industry / 行业分析"
                changes_count += 1

    print(f"Standardized {changes_count} categories.")

    # Check if we need to add new questions
    # The original file had 174 questions. If we have significantly more, we probably already added them.
    # A safer check is to see if we have questions for Chapter 9.
    has_chapter_9 = any(q['category'].startswith("9. Financial Ethics") for q in questions)
    
    if not has_chapter_9:
        print("Adding new questions...")
        
        # Determine next ID
        max_id = 0
        if questions:
            max_id = max(q['id'] for q in questions)
        
        current_id = max_id + 1
        
        for q in NEW_QUESTIONS:
            q['id'] = current_id
            questions.append(q)
            current_id += 1
    else:
        print("Chapter 9 questions already exist. Skipping addition.")

    # Save file
    print("Saving questions.json...")
    with open(JSON_PATH, 'w', encoding='utf-8') as f:
        json.dump(questions, f, ensure_ascii=False, indent=2)

    # Print distribution
    print("\nNew Question Distribution:")
    distribution = {}
    for q in questions:
        cat = q['category']
        distribution[cat] = distribution.get(cat, 0) + 1
    
    for cat in sorted(distribution.keys()):
        print(f"{cat}: {distribution[cat]}")

    print(f"\nTotal questions: {len(questions)}")

if __name__ == "__main__":
    main()
