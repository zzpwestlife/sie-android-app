import json
import os

# Define the new questions
new_questions_data = [
    # --- STOCKS (25 Questions) ---
    {
        "content": "What is the primary difference between Common Stock and Preferred Stock regarding voting rights?\n普通股和优先股在投票权方面的主要区别是什么？",
        "options": [
            "Preferred stockholders usually have voting rights, while common stockholders do not.\n优先股股东通常有投票权，而普通股股东没有。",
            "Common stockholders usually have voting rights, while preferred stockholders do not.\n普通股股东通常有投票权，而优先股股东没有。",
            "Both have equal voting rights.\n两者拥有同等的投票权。"
        ],
        "correctAnswerIndex": 1,
        "explanation": "Common stockholders typically have voting rights to elect the board of directors, while preferred stockholders usually do not have voting rights but have priority in dividends.\n普通股股东通常拥有选举董事会的投票权，而优先股股东通常没有投票权，但在股息分配上享有优先权。",
        "category": "2. Stocks / 股票"
    },
    {
        "content": "Which market is where securities are created and sold for the first time?\n证券首次发行和出售的市场是哪个？",
        "options": [
            "Secondary Market\n二级市场",
            "Primary Market\n一级市场",
            "Over-the-Counter Market\n场外交易市场"
        ],
        "correctAnswerIndex": 1,
        "explanation": "The Primary Market is where new securities are issued (e.g., IPOs). The Secondary Market is where existing securities are traded among investors.\n一级市场是新证券发行（如IPO）的地方。二级市场是投资者之间交易现有证券的地方。",
        "category": "2. Stocks / 股票"
    },
    {
        "content": "What does an ADR (American Depositary Receipt) represent?\nADR（美国存托凭证）代表什么？",
        "options": [
            "A bond issued by the US government.\n美国政府发行的债券。",
            "Shares of a non-US company that trade in the US financial markets.\n在美国金融市场上交易的非美国公司的股票。",
            "A derivative contract based on the S&P 500.\n基于标普500的衍生品合约。"
        ],
        "correctAnswerIndex": 1,
        "explanation": "ADRs allow US investors to buy shares in foreign companies without having to purchase shares on foreign exchanges.\nADR允许美国投资者购买外国公司的股票，而无需在外国交易所购买股票。",
        "category": "2. Stocks / 股票"
    },
    {
        "content": "Which date determines which shareholders are entitled to receive a declared dividend?\n哪一天决定了哪些股东有权获得已宣告的股息？",
        "options": [
            "Declaration Date\n宣告日",
            "Ex-Dividend Date\n除息日",
            "Record Date\n股权登记日"
        ],
        "correctAnswerIndex": 2,
        "explanation": "The Record Date is the date established by the issuer. An investor must be a shareholder of record on this date to receive the dividend.\n股权登记日是发行人确定的日期。投资者必须在这一天成为登记在册的股东才能获得股息。",
        "category": "2. Stocks / 股票"
    },
    {
        "content": "What happens to the price of a stock on the Ex-Dividend Date?\n在除息日，股票价格会发生什么变化？",
        "options": [
            "It increases by the amount of the dividend.\n价格上涨，幅度为股息金额。",
            "It decreases by the amount of the dividend.\n价格下跌，幅度为股息金额。",
            "It remains unchanged.\n价格保持不变。"
        ],
        "correctAnswerIndex": 1,
        "explanation": "On the ex-dividend date, the stock price is adjusted downward by the amount of the dividend because new buyers are not entitled to the payout.\n在除息日，股价会根据股息金额向下调整，因为新买家无权获得该股息。",
        "category": "2. Stocks / 股票"
    },
    {
        "content": "What is the primary purpose of a Forward Stock Split (e.g., 2-for-1)?\n正向股票拆股（如2拆1）的主要目的是什么？",
        "options": [
            "To increase the market price per share.\n提高每股市场价格。",
            "To decrease the number of outstanding shares.\n减少流通股数量。",
            "To reduce the market price per share to make it more affordable.\n降低每股市场价格，使其更易于购买。"
        ],
        "correctAnswerIndex": 2,
        "explanation": "A forward split increases the number of shares and proportionally decreases the price, making the stock more accessible to retail investors.\n正向拆股增加股份数量并按比例降低价格，使散户投资者更容易购买股票。",
        "category": "2. Stocks / 股票"
    },
    {
        "content": "How does a Reverse Stock Split affect the number of shares and the price?\n反向股票拆股如何影响股份数量和价格？",
        "options": [
            "Increases shares, decreases price.\n增加股份，降低价格。",
            "Decreases shares, increases price.\n减少股份，提高价格。",
            "Increases shares, increases price.\n增加股份，提高价格。"
        ],
        "correctAnswerIndex": 1,
        "explanation": "A reverse split consolidates shares (fewer shares) and increases the price per share proportionally.\n反向拆股合并股份（股份减少），并按比例提高每股价格。",
        "category": "2. Stocks / 股票"
    },
    {
        "content": "What is a Stock Buyback (Share Repurchase)?\n什么是股票回购？",
        "options": [
            "When a company sells more shares to the public.\n当公司向公众出售更多股票时。",
            "When a company buys its own outstanding shares from the market.\n当公司从市场上买回自己的流通股时。",
            "When an investor buys shares from another investor.\n当投资者从另一位投资者手中购买股票时。"
        ],
        "correctAnswerIndex": 1,
        "explanation": "Buybacks reduce the number of outstanding shares, which often increases Earnings Per Share (EPS).\n回购减少了流通股的数量，通常会增加每股收益（EPS）。",
        "category": "2. Stocks / 股票"
    },
    {
        "content": "What does EPS stand for and how is it calculated?\nEPS代表什么，它是如何计算的？",
        "options": [
            "Earnings Per Share = Net Income / Shares Outstanding\n每股收益 = 净利润 / 流通股数",
            "Equity Per Share = Total Equity / Shares Outstanding\n每股权益 = 总权益 / 流通股数",
            "Earnings Price Share = Price / Earnings\n收益价格比 = 价格 / 收益"
        ],
        "correctAnswerIndex": 0,
        "explanation": "EPS (Earnings Per Share) is a company's net profit divided by the number of common shares it has outstanding.\nEPS（每股收益）是公司的净利润除以其流通在外的普通股数量。",
        "category": "2. Stocks / 股票"
    },
    {
        "content": "What does the P/E Ratio (Price-to-Earnings) indicate?\n市盈率（P/E Ratio）表明什么？",
        "options": [
            "The company's debt level relative to its equity.\n公司的债务水平相对于其权益的比例。",
            "How much investors are willing to pay per dollar of earnings.\n投资者愿意为每一美元的收益支付多少价格。",
            "The dividend yield of the stock.\n股票的股息收益率。"
        ],
        "correctAnswerIndex": 1,
        "explanation": "The P/E ratio measures the current share price relative to its per-share earnings.\n市盈率衡量当前股价相对于每股收益的比例。",
        "category": "2. Stocks / 股票"
    },
    {
        "content": "Which type of stock typically has the highest priority in the event of liquidation?\n在清算时，哪种类型的股票通常具有最高的优先权？",
        "options": [
            "Common Stock\n普通股",
            "Preferred Stock\n优先股",
            "Penny Stock\n仙股"
        ],
        "correctAnswerIndex": 1,
        "explanation": "Preferred stockholders have a higher claim on assets than common stockholders during liquidation (though lower than bondholders).\n在清算期间，优先股股东对资产的索取权高于普通股股东（但低于债券持有人）。",
        "category": "2. Stocks / 股票"
    },
    {
        "content": "What is the formula for Dividend Yield?\n股息收益率的公式是什么？",
        "options": [
            "Annual Dividend / Current Stock Price\n年度股息 / 当前股价",
            "Current Stock Price / Annual Dividend\n当前股价 / 年度股息",
            "Net Income / Annual Dividend\n净利润 / 年度股息"
        ],
        "correctAnswerIndex": 0,
        "explanation": "Dividend Yield is calculated as the annual dividend payment divided by the current stock price.\n股息收益率的计算方法是年度股息支付额除以当前股价。",
        "category": "2. Stocks / 股票"
    },
    {
        "content": "What is a 'Blue Chip' stock?\n什么是“蓝筹”股？",
        "options": [
            "A high-risk, low-priced stock.\n高风险、低价格的股票。",
            "Stock of a large, well-established, and financially sound company.\n大型、信誉良好且财务稳健的公司的股票。",
            "A stock that only pays dividends in blue currency.\n只用蓝色货币支付股息的股票。"
        ],
        "correctAnswerIndex": 1,
        "explanation": "Blue Chip stocks are shares of huge, well-reputed companies with a history of reliable earnings and often dividends.\n蓝筹股是指那些规模巨大、声誉良好、盈利可靠且通常支付股息的公司的股票。",
        "category": "2. Stocks / 股票"
    },
    {
        "content": "What does ROE (Return on Equity) measure?\nROE（净资产收益率）衡量什么？",
        "options": [
            "How much profit a company generates with its shareholders' equity.\n公司利用股东权益产生了多少利润。",
            "The total revenue of the company.\n公司的总收入。",
            "The return on the company's debt investments.\n公司债务投资的回报。"
        ],
        "correctAnswerIndex": 0,
        "explanation": "ROE measures financial performance by dividing net income by shareholders' equity.\nROE通过将净利润除以股东权益来衡量财务绩效。",
        "category": "2. Stocks / 股票"
    },
    {
        "content": "What is a 'Growth Stock'?\n什么是“成长股”？",
        "options": [
            "A stock that pays high dividends.\n支付高股息的股票。",
            "A stock expected to grow at an above-average rate compared to other stocks.\n预期增长率高于其他股票平均水平的股票。",
            "A stock with a very low P/E ratio.\n市盈率非常低的股票。"
        ],
        "correctAnswerIndex": 1,
        "explanation": "Growth stocks are companies expected to increase revenue and earnings faster than the market average, often reinvesting earnings rather than paying dividends.\n成长股是指预期收入和收益增长快于市场平均水平的公司，通常会将收益再投资而不是支付股息。",
        "category": "2. Stocks / 股票"
    },
    {
        "content": "What is a 'Value Stock'?\n什么是“价值股”？",
        "options": [
            "A stock that trades at a lower price relative to its fundamentals (dividends, earnings, sales).\n相对于其基本面（股息、收益、销售额）交易价格较低的股票。",
            "A stock with the highest market capitalization.\n市值最高的股票。",
            "A stock in the technology sector.\n科技板块的股票。"
        ],
        "correctAnswerIndex": 0,
        "explanation": "Value stocks are considered undervalued by the market and often have lower P/E ratios and higher dividend yields.\n价值股被市场认为被低估，通常具有较低的市盈率和较高的股息收益率。",
        "category": "2. Stocks / 股票"
    },
    {
        "content": "What does Market Capitalization represent?\n市值代表什么？",
        "options": [
            "The total value of a company's assets.\n公司资产的总价值。",
            "The total market value of a company's outstanding shares.\n公司流通股的总市场价值。",
            "The price of a single share.\n单股的价格。"
        ],
        "correctAnswerIndex": 1,
        "explanation": "Market Cap = Current Share Price × Total Number of Outstanding Shares.\n市值 = 当前股价 × 流通股总数。",
        "category": "2. Stocks / 股票"
    },
    {
        "content": "What is 'Statutory Voting'?\n什么是“法定投票”？",
        "options": [
            "Shareholders can cast all their votes for one director candidate.\n股东可以将所有选票投给一名董事候选人。",
            "Shareholders must divide their votes evenly among all candidates.\n股东必须将选票平均分配给所有候选人。",
            "Shareholders can cast one vote per share for each director position open for election.\n股东可以为每个待选董事职位投每股一票。"
        ],
        "correctAnswerIndex": 2,
        "explanation": "Under Statutory Voting, you have a set number of votes per share for each seat and cannot accumulate them for one candidate.\n在法定投票下，你拥有的每一股对每个席位都有固定的票数，不能累积投给一个候选人。",
        "category": "2. Stocks / 股票"
    },
    {
        "content": "What is 'Cumulative Voting'?\n什么是“累积投票”？",
        "options": [
            "Shareholders can pool their votes and cast them all for a single director candidate.\n股东可以汇集他们的选票，全部投给一名董事候选人。",
            "Shareholders cannot vote for directors.\n股东不能投票选举董事。",
            "Votes are only counted if the shareholder attends the meeting.\n只有股东出席会议时才计算选票。"
        ],
        "correctAnswerIndex": 0,
        "explanation": "Cumulative voting benefits minority shareholders by allowing them to concentrate their votes on one candidate.\n累积投票允许少数股东将选票集中投给一名候选人，从而有利于他们。",
        "category": "2. Stocks / 股票"
    },
    {
        "content": "What is a 'Defensive Stock'?\n什么是“防御型股票”？",
        "options": [
            "A stock that performs well during economic expansions but poorly in recessions.\n在经济扩张期表现良好但在衰退期表现不佳的股票。",
            "A stock that provides consistent dividends and stable earnings regardless of the state of the stock market.\n无论股市状况如何，都能提供持续股息和稳定收益的股票。",
            "A stock related to the defense industry.\n与国防工业相关的股票。"
        ],
        "correctAnswerIndex": 1,
        "explanation": "Defensive stocks (e.g., utilities, consumer staples) tend to remain stable during economic downturns.\n防御型股票（如公用事业、日用消费品）往往在经济低迷时期保持稳定。",
        "category": "2. Stocks / 股票"
    },
    {
        "content": "What is the P/B Ratio (Price-to-Book)?\n市净率（P/B Ratio）是什么？",
        "options": [
            "Market Price per Share / Book Value per Share\n每股市价 / 每股账面价值",
            "Market Price per Share / Earnings per Share\n每股市价 / 每股收益",
            "Book Value per Share / Market Price per Share\n每股账面价值 / 每股市价"
        ],
        "correctAnswerIndex": 0,
        "explanation": "The P/B ratio compares a firm's market capitalization to its book value (net assets).\n市净率将公司的市值与其账面价值（净资产）进行比较。",
        "category": "2. Stocks / 股票"
    },
    {
        "content": "What are 'Outstanding Shares'?\n什么是“流通股”？",
        "options": [
            "Shares authorized but not yet issued.\n已授权但尚未发行的股票。",
            "Shares held by investors, including restricted shares owned by company officers and insiders.\n投资者持有的股票，包括公司高管和内部人士持有的限制性股票。",
            "Shares that the company has repurchased (Treasury Stock).\n公司已回购的股票（库存股）。"
        ],
        "correctAnswerIndex": 1,
        "explanation": "Outstanding shares are all shares currently held by all shareholders. Authorized shares minus Treasury shares equals Outstanding shares.\n流通股是目前所有股东持有的所有股票。授权股份减去库存股等于流通股。",
        "category": "2. Stocks / 股票"
    },
    {
        "content": "What is 'Treasury Stock'?\n什么是“库存股”？",
        "options": [
            "Stock issued by the US Treasury.\n美国财政部发行的股票。",
            "Stock that a company has bought back from the open market.\n公司从公开市场回购的股票。",
            "Stock held by the company's treasurer.\n公司财务主管持有的股票。"
        ],
        "correctAnswerIndex": 1,
        "explanation": "Treasury stock is previously outstanding stock that is bought back by the issuing company. It has no voting rights and receives no dividends.\n库存股是发行公司回购的以前流通的股票。它没有投票权，也不获得股息。",
        "category": "2. Stocks / 股票"
    },
    {
        "content": "What is the primary risk associated with ADRs specifically?\n与ADR（美国存托凭证）特别相关的主要风险是什么？",
        "options": [
            "Interest Rate Risk\n利率风险",
            "Currency/Exchange Rate Risk\n货币/汇率风险",
            "Inflation Risk\n通货膨胀风险"
        ],
        "correctAnswerIndex": 1,
        "explanation": "Since the underlying stock is valued in a foreign currency, changes in the exchange rate can affect the value of the ADR in US dollars.\n由于标的股票以主要外币计价，汇率变化会影响ADR的美元价值。",
        "category": "2. Stocks / 股票"
    },
    {
        "content": "Which term describes the last price at which a stock traded?\n哪个术语描述了股票交易的最新价格？",
        "options": [
            "Bid Price\n买入价",
            "Ask Price\n卖出价",
            "Market Price / Last Trade Price\n市场价 / 最新成交价"
        ],
        "correctAnswerIndex": 2,
        "explanation": "The market price is the most recent price at which a trade was executed.\n市场价格是最近一次交易执行的价格。",
        "category": "2. Stocks / 股票"
    },

    # --- ETFs (25 Questions) ---
    {
        "content": "What does ETF stand for?\nETF代表什么？",
        "options": [
            "Exchange Traded Fund\n交易所交易基金",
            "Equity Transfer Fund\n股票转让基金",
            "Electronic Trade Facility\n电子交易设施"
        ],
        "correctAnswerIndex": 0,
        "explanation": "An ETF is an investment fund traded on stock exchanges, much like stocks.\nETF是一种在证券交易所交易的投资基金，就像股票一样。",
        "category": "3. ETFs / 交易所交易基金"
    },
    {
        "content": "What is the primary mechanism that keeps an ETF's market price close to its Net Asset Value (NAV)?\n使ETF的市场价格接近其净资产价值（NAV）的主要机制是什么？",
        "options": [
            "The Federal Reserve's intervention.\n美联储的干预。",
            "The Creation and Redemption mechanism.\n申购和赎回机制。",
            "Fixed exchange rates.\n固定汇率。"
        ],
        "correctAnswerIndex": 1,
        "explanation": "Authorized Participants (APs) use the creation/redemption mechanism to arbitrage differences between price and NAV, keeping them aligned.\n授权参与者（AP）利用申购/赎回机制套利价格与NAV之间的差异，使其保持一致。",
        "category": "3. ETFs / 交易所交易基金"
    },
    {
        "content": "Who are the only entities allowed to create or redeem ETF shares directly with the fund sponsor?\n谁是唯一被允许直接与基金发起人申购或赎回ETF份额的实体？",
        "options": [
            "Retail Investors\n散户投资者",
            "Authorized Participants (APs)\n授权参与者 (AP)",
            "Financial Advisors\n理财顾问"
        ],
        "correctAnswerIndex": 1,
        "explanation": "Only Authorized Participants (usually large institutional investors) can deal directly with the ETF issuer to create or redeem shares in large blocks.\n只有授权参与者（通常是大型机构投资者）才能直接与ETF发行人进行大宗份额的申购或赎回。",
        "category": "3. ETFs / 交易所交易基金"
    },
    {
        "content": "What is 'In-Kind' creation/redemption in ETFs?\nETF中的“实物”申购/赎回是什么？",
        "options": [
            "Exchanging cash for ETF shares.\n用现金兑换ETF份额。",
            "Exchanging a basket of underlying securities for ETF shares (or vice versa).\n用一篮子标的证券兑换ETF份额（或反之亦然）。",
            "Exchanging gold for ETF shares.\n用黄金兑换ETF份额。"
        ],
        "correctAnswerIndex": 1,
        "explanation": "In-kind transactions involve exchanging the actual securities held by the fund, which helps minimize tax implications.\n实物交易涉及交换基金持有的实际证券，这有助于最大限度地减少税务影响。",
        "category": "3. ETFs / 交易所交易基金"
    },
    {
        "content": "Compared to Mutual Funds, what is a key trading advantage of ETFs?\n与共同基金相比，ETF的主要交易优势是什么？",
        "options": [
            "They can be traded throughout the day like stocks.\n它们可以像股票一样全天交易。",
            "They guarantee a higher return.\n它们保证更高的回报。",
            "They have no management fees.\n它们没有管理费。"
        ],
        "correctAnswerIndex": 0,
        "explanation": "ETFs trade on an exchange and can be bought/sold intraday, whereas mutual funds trade only once a day at the closing NAV.\nETF在交易所交易，可以日内买卖，而共同基金每天仅按收盘NAV交易一次。",
        "category": "3. ETFs / 交易所交易基金"
    },
    {
        "content": "What is the 'Expense Ratio' of an ETF?\nETF的“费用比率”是什么？",
        "options": [
            "The commission paid to buy the ETF.\n购买ETF支付的佣金。",
            "The annual fee charged by the fund to cover management and operating costs.\n基金收取的用于支付管理和运营成本的年费。",
            "The ratio of winners to losers in the fund.\n基金中赢家与输家的比例。"
        ],
        "correctAnswerIndex": 1,
        "explanation": "The expense ratio is expressed as a percentage of assets deducted annually to pay for fund expenses.\n费用比率表示为每年扣除的用于支付基金费用的资产百分比。",
        "category": "3. ETFs / 交易所交易基金"
    },
    {
        "content": "What does AUM stand for in the context of ETFs?\n在ETF背景下，AUM代表什么？",
        "options": [
            "Assets Under Management\n管理资产规模",
            "Average Unit Market\n平均单位市场",
            "Annual Underlying Margin\n年度标的保证金"
        ],
        "correctAnswerIndex": 0,
        "explanation": "AUM (Assets Under Management) is the total market value of the investments that the ETF manages.\nAUM（管理资产规模）是ETF管理的投资总市值。",
        "category": "3. ETFs / 交易所交易基金"
    },
    {
        "content": "What is an Inverse ETF designed to do?\n反向ETF的设计目的是什么？",
        "options": [
            "Amplify the returns of an index (e.g., 2x).\n放大指数的回报（如2倍）。",
            "Deliver the opposite performance of the index it tracks.\n提供与其跟踪指数相反的表现。",
            "Provide a guaranteed income stream.\n提供有保证的收入流。"
        ],
        "correctAnswerIndex": 1,
        "explanation": "Inverse ETFs use derivatives to profit from a decline in the value of an underlying benchmark.\n反向ETF利用衍生品从标的基准价值的下跌中获利。",
        "category": "3. ETFs / 交易所交易基金"
    },
    {
        "content": "What is a Leveraged ETF?\n什么是杠杆ETF？",
        "options": [
            "An ETF that uses debt or derivatives to amplify the returns of an index.\n使用债务或衍生品来放大指数回报的ETF。",
            "An ETF that invests only in bank loans.\n只投资于银行贷款的ETF。",
            "An ETF with very low fees.\n费用非常低的ETF。"
        ],
        "correctAnswerIndex": 0,
        "explanation": "Leveraged ETFs aim to return a multiple (e.g., 2x or 3x) of the underlying index's daily return.\n杠杆ETF旨在提供标的指数单日回报的倍数（如2倍或3倍）。",
        "category": "3. ETFs / 交易所交易基金"
    },
    {
        "content": "What is 'Volatility Decay' (or Beta Slippage) in Leveraged/Inverse ETFs?\n杠杆/反向ETF中的“波动损耗”（或Beta滑点）是什么？",
        "options": [
            "The fees paid to the manager.\n支付给经理的费用。",
            "The erosion of value over time due to daily rebalancing, especially in volatile markets.\n由于每日再平衡导致的价值随时间侵蚀，特别是在波动市场中。",
            "The decay of the physical assets held.\n持有实物资产的腐烂。"
        ],
        "correctAnswerIndex": 1,
        "explanation": "Because leveraged ETFs reset daily, holding them for longer periods in a volatile market can result in returns significantly different from the expected multiple of the long-term index return.\n由于杠杆ETF每日重置，在波动市场中长期持有它们可能导致回报与预期的长期指数回报倍数存在显著差异。",
        "category": "3. ETFs / 交易所交易基金"
    },
    {
        "content": "Why are Leveraged ETFs generally considered unsuitable for long-term buy-and-hold investors?\n为什么杠杆ETF通常被认为不适合长期买入持有投资者？",
        "options": [
            "They have high expense ratios and volatility decay.\n它们具有高费用比率和波动损耗。",
            "They cannot be sold easily.\n它们不容易出售。",
            "They do not track any index.\n它们不跟踪任何指数。"
        ],
        "correctAnswerIndex": 0,
        "explanation": "Due to the daily reset mechanism, long-term performance usually degrades, making them tactical trading tools rather than long-term investments.\n由于每日重置机制，长期表现通常会下降，使其成为战术交易工具而非长期投资。",
        "category": "3. ETFs / 交易所交易基金"
    },
    {
        "content": "What implies high liquidity in an ETF?\n什么意味着ETF具有高流动性？",
        "options": [
            "Wide bid-ask spreads.\n宽的买卖价差。",
            "Narrow bid-ask spreads and high trading volume.\n窄的买卖价差和高交易量。",
            "Low assets under management.\n低管理资产规模。"
        ],
        "correctAnswerIndex": 1,
        "explanation": "High liquidity allows investors to buy or sell shares quickly with minimal price impact, characterized by tight spreads and high volume.\n高流动性允许投资者以最小的价格影响快速买卖份额，其特征是价差小和交易量大。",
        "category": "3. ETFs / 交易所交易基金"
    },
    {
        "content": "Can ETFs pay dividends?\nETF可以支付股息吗？",
        "options": [
            "No, never.\n不，从来不。",
            "Yes, if the underlying securities pay dividends, the ETF passes them to shareholders.\n是的，如果标的证券支付股息，ETF会将其传递给股东。",
            "Only if the ETF is leveraged.\n只有当ETF是杠杆型时。"
        ],
        "correctAnswerIndex": 1,
        "explanation": "ETFs collect dividends from the stocks they hold and distribute them to shareholders, typically quarterly.\nETF收集其持有的股票的股息，并将其分发给股东，通常按季度分发。",
        "category": "3. ETFs / 交易所交易基金"
    },
    {
        "content": "What is an 'Active ETF'?\n什么是“主动型ETF”？",
        "options": [
            "An ETF that simply tracks an index like the S&P 500.\n简单跟踪标普500等指数的ETF。",
            "An ETF where a portfolio manager actively selects securities to try to outperform the market.\n由投资组合经理主动选择证券以试图跑赢市场的ETF。",
            "An ETF that trades 24 hours a day.\n全天24小时交易的ETF。"
        ],
        "correctAnswerIndex": 1,
        "explanation": "Active ETFs do not follow a passive index rule; managers make decisions on what to buy/sell to generate alpha.\n主动型ETF不遵循被动指数规则；经理决定买入/卖出什么以产生超额收益（Alpha）。",
        "category": "3. ETFs / 交易所交易基金"
    },
    {
        "content": "What is the 'Spread' in ETF trading?\nETF交易中的“价差”是什么？",
        "options": [
            "The difference between the Bid and Ask prices.\n买入价和卖出价之间的差额。",
            "The difference between the high and low of the day.\n当天最高价和最低价的差额。",
            "The fee paid to the broker.\n支付给经纪人的费用。"
        ],
        "correctAnswerIndex": 0,
        "explanation": "The Bid-Ask spread is the cost of trading and reflects liquidity. Tighter spreads are better for investors.\n买卖价差是交易成本，反映了流动性。价差越小对投资者越有利。",
        "category": "3. ETFs / 交易所交易基金"
    },
    {
        "content": "What is 'Tracking Error' in an ETF?\nETF中的“跟踪误差”是什么？",
        "options": [
            "A mistake by the broker.\n经纪人的错误。",
            "The divergence between the ETF's performance and the performance of its underlying index.\nETF的表现与其标的指数表现之间的偏差。",
            "When the ETF price goes down.\n当ETF价格下跌时。"
        ],
        "correctAnswerIndex": 1,
        "explanation": "Tracking error measures how consistently the ETF follows its benchmark. Lower tracking error is preferred for passive ETFs.\n跟踪误差衡量ETF跟随其基准的一致性。对于被动ETF，跟踪误差越低越好。",
        "category": "3. ETFs / 交易所交易基金"
    },
    {
        "content": "What is a 'Sector ETF'?\n什么是“板块ETF”？",
        "options": [
            "An ETF that invests in the entire market.\n投资于整个市场的ETF。",
            "An ETF that focuses on a specific industry or sector (e.g., Technology, Healthcare).\n专注于特定行业或板块（如科技、医疗）的ETF。",
            "An ETF that holds only bonds.\n只持有债券的ETF。"
        ],
        "correctAnswerIndex": 1,
        "explanation": "Sector ETFs allow investors to gain exposure to specific parts of the economy.\n板块ETF允许投资者获得对经济特定部分的敞口。",
        "category": "3. ETFs / 交易所交易基金"
    },
    {
        "content": "What is a 'Bond ETF'?\n什么是“债券ETF”？",
        "options": [
            "An ETF that invests in fixed-income securities.\n投资于固定收益证券的ETF。",
            "An ETF that is guaranteed by the government.\n由政府担保的ETF。",
            "An ETF that has a maturity date like a bond.\n像债券一样有到期日的ETF。"
        ],
        "correctAnswerIndex": 0,
        "explanation": "Bond ETFs hold a portfolio of bonds. Unlike individual bonds, most bond ETFs do not have a maturity date and trade continuously.\n债券ETF持有债券组合。与个别债券不同，大多数债券ETF没有到期日，并且持续交易。",
        "category": "3. ETFs / 交易所交易基金"
    },
    {
        "content": "What is the tax efficiency benefit of the ETF creation/redemption process?\nETF申购/赎回过程的税收效率优势是什么？",
        "options": [
            "ETFs are tax-exempt.\nETF是免税的。",
            "In-kind redemptions allow the fund to offload low-cost basis shares without triggering capital gains taxes for the fund.\n实物赎回允许基金剥离低成本基础的股票，而不会触发基金的资本利得税。",
            "Dividends are not taxed.\n股息不征税。"
        ],
        "correctAnswerIndex": 1,
        "explanation": "This mechanism minimizes capital gains distributions to shareholders, making ETFs generally more tax-efficient than mutual funds.\n这种机制最大限度地减少了向股东分配的资本利得，使ETF通常比共同基金更具税收效率。",
        "category": "3. ETFs / 交易所交易基金"
    },
    {
        "content": "What is the 'Intraday Indicative Value' (IIV) of an ETF?\nETF的“日内指示性价值”（IIV）是什么？",
        "options": [
            "The closing price.\n收盘价。",
            "A real-time estimate of the ETF's fair value based on its underlying assets, updated every 15 seconds.\n基于其标的资产的ETF公允价值的实时估计，每15秒更新一次。",
            "The maximum price of the day.\n当天的最高价。"
        ],
        "correctAnswerIndex": 1,
        "explanation": "IIV helps investors see if the ETF is trading at a premium or discount to its NAV during the trading day.\nIIV帮助投资者在交易日内查看ETF的交易价格是溢价还是折价于其NAV。",
        "category": "3. ETFs / 交易所交易基金"
    },
    {
        "content": "Can you buy an ETF on margin (borrowed money)?\n可以用保证金（借来的钱）购买ETF吗？",
        "options": [
            "Yes, because they trade like stocks.\n是的，因为它们像股票一样交易。",
            "No, it is prohibited by the SEC.\n不，这是SEC禁止的。",
            "Only if it is a bond ETF.\n只有当它是债券ETF时。"
        ],
        "correctAnswerIndex": 0,
        "explanation": "Since ETFs trade on exchanges like equities, they are marginable securities.\n由于ETF像股票一样在交易所交易，它们是可保证金交易的证券。",
        "category": "3. ETFs / 交易所交易基金"
    },
    {
        "content": "What is a 'Commodity ETF'?\n什么是“商品ETF”？",
        "options": [
            "An ETF that invests in physical goods like gold, oil, or corn.\n投资于黄金、石油或玉米等实物商品的ETF。",
            "An ETF that invests in commodity-producing companies only.\n只投资于商品生产公司的ETF。",
            "An ETF that is used to buy groceries.\n用于购买杂货的ETF。"
        ],
        "correctAnswerIndex": 0,
        "explanation": "Commodity ETFs track the price of a commodity, either by holding the physical asset or through futures contracts.\n商品ETF通过持有实物资产或期货合约来跟踪商品的价格。",
        "category": "3. ETFs / 交易所交易基金"
    },
    {
        "content": "What risk is introduced when an ETF uses futures contracts (contango)?\n当ETF使用期货合约（升水）时会引入什么风险？",
        "options": [
            "Roll Yield Risk / Contango\n展期收益风险 / 升水",
            "Credit Risk\n信用风险",
            "Interest Rate Risk\n利率风险"
        ],
        "correctAnswerIndex": 0,
        "explanation": "If futures prices are higher than spot prices (contango), the ETF loses money when rolling over expiring contracts to more expensive new ones.\n如果期货价格高于现货价格（升水），ETF在将到期合约展期为更昂贵的新合约时会亏损。",
        "category": "3. ETFs / 交易所交易基金"
    },
    {
        "content": "What is a 'Synthetic ETF'?\n什么是“合成ETF”？",
        "options": [
            "An ETF that holds the actual physical securities.\n持有实际实物证券的ETF。",
            "An ETF that uses derivatives (swaps) to track an index instead of holding the underlying assets.\n使用衍生品（互换）来跟踪指数而不是持有标的资产的ETF。",
            "An ETF made of plastic.\n用塑料制成的ETF。"
        ],
        "correctAnswerIndex": 1,
        "explanation": "Synthetic ETFs rely on counterparty promises (swaps) to deliver returns, introducing counterparty risk.\n合成ETF依赖交易对手的承诺（互换）来提供回报，从而引入了交易对手风险。",
        "category": "3. ETFs / 交易所交易基金"
    },
    {
        "content": "Which of the following generally has lower expense ratios?\n以下哪种通常具有较低的费用比率？",
        "options": [
            "Active ETFs\n主动型ETF",
            "Passive / Index ETFs\n被动/指数型ETF",
            "Hedge Funds\n对冲基金"
        ],
        "correctAnswerIndex": 1,
        "explanation": "Passive ETFs simply replicate an index and require less management, leading to lower fees.\n被动ETF只是复制指数，需要较少的管理，从而导致费用较低。",
        "category": "3. ETFs / 交易所交易基金"
    }
]

file_path = 'core/database/src/main/assets/questions.json'

# Read existing file
try:
    with open(file_path, 'r', encoding='utf-8') as f:
        existing_questions = json.load(f)
except FileNotFoundError:
    print(f"File not found: {file_path}")
    existing_questions = []

# Find max ID
max_id = 0
if existing_questions:
    max_id = max(q.get('id', 0) for q in existing_questions)

print(f"Current Max ID: {max_id}")

# Append new questions
start_id = max_id + 1
for i, question in enumerate(new_questions_data):
    question['id'] = start_id + i
    existing_questions.append(question)

# Write back
with open(file_path, 'w', encoding='utf-8') as f:
    json.dump(existing_questions, f, indent=2, ensure_ascii=False)

print(f"Successfully appended {len(new_questions_data)} new questions.")
print(f"New Max ID: {start_id + len(new_questions_data) - 1}")
