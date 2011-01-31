#!/usr/bin/python
#
# Copyright (C) 2009 Tan Swee Heng
#
# Licensed under the Apache License, Version 2.0 (the "License");
# you may not use this file except in compliance with the License.
# You may obtain a copy of the License at
#
#      http://www.apache.org/licenses/LICENSE-2.0
#
# Unless required by applicable law or agreed to in writing, software
# distributed under the License is distributed on an "AS IS" BASIS,
# WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
# See the License for the specific language governing permissions and
# limitations under the License.


__author__ = 'thesweeheng@gmail.com'


from gdata.finance.service import \
    FinanceService, PortfolioQuery, PositionQuery
from gdata.finance import \
    PortfolioEntry, PortfolioData, TransactionEntry, TransactionData, \
    Price, Commission, Money
import datetime
import sys


def PrintReturns(pfx, d):
  """Print returns."""
  print pfx, '%1.5f(1w)  %1.5f(4w)  %1.5f(3m)  %1.5f(YTD)' % tuple(
    float(i) for i in (d.return1w, d.return4w, d.return3m, d.returnYTD))
  pfx = ' ' * len(pfx)
  print pfx, '%1.5f(1y)  %1.5f(3y)  %1.5f(5y)  %1.5f(overall)' % tuple(
    float(i) for i in (d.return1y, d.return3y, d.return5y, d.return_overall))


PrRtn = PrintReturns


def PrintTransactions(transactions):
  """Print transactions."""
  print "    Transactions:"
  fmt = '    %4s %-23s %-10s %6s %-11s %-11s'
  print fmt % ('ID','Date','Type','Shares','Price','Commission')
  for txn in transactions:
    d = txn.transaction_data
    print fmt % (txn.transaction_id, d.date or '----', d.type,
        d.shares, d.price.money[0], d.commission.money[0])
    if d.notes:
        print "         Notes:", d.notes
  print


def PrintPosition(pos, with_returns=False):
  """Print single position."""
  print '    Position      :', pos.position_title
  print '    Ticker ID     :', pos.ticker_id
  print '    Symbol        :', pos.symbol
  print '    Last updated  :', pos.updated.text
  d = pos.position_data
  print '    Shares        :', d.shares
  if with_returns:
    print '    Gain %        :', d.gain_percentage
    PrRtn('    Returns       :', d)
    print '    Cost basis    :', d.cost_basis
    print '    Days gain     :', d.days_gain
    print '    Gain          :', d.gain
    print '    Market value  :', d.market_value
  print
  if pos.transactions:
    print "    <inlined transactions>\n"
    PrintTransactions(pos.transactions)
    print "    </inlined transactions>\n"


def PrintPositions(positions, with_returns=False):
  for pos in positions:
    PrintPosition(pos, with_returns)


def PrintPortfolio(pfl, with_returns=False):
  """Print single portfolio."""
  print 'Portfolio Title   :', pfl.portfolio_title
  print 'Portfolio ID      :', pfl.portfolio_id
  print '  Last updated    :', pfl.updated.text
  d = pfl.portfolio_data
  print '  Currency        :', d.currency_code
  if with_returns:
    print '  Gain %          :', d.gain_percentage
    PrRtn('  Returns         :', d)
    print '  Cost basis      :', d.cost_basis
    print '  Days gain       :', d.days_gain
    print '  Gain            :', d.gain
    print '  Market value    :', d.market_value
  print
  if pfl.positions:
    print "  <inlined positions>\n"
    PrintPositions(pfl.positions, with_returns)
    print "  </inlined positions>\n"


def PrintPortfolios(portfolios, with_returns=False):
  for pfl in portfolios:
    PrintPortfolio(pfl, with_returns)


def ShowCallDetails(meth):
  def wrap(*args, **kwargs):
    print '@', meth.__name__, args[1:], kwargs
    meth(*args, **kwargs)
  return wrap


class FinanceTester(object):

  def __init__(self, email, password):
    self.client = FinanceService(source='gdata-finance-test')
    self.client.ClientLogin(email, password)
  
  def GetPortfolios(self, with_returns=False, inline_positions=False): 
    query = PortfolioQuery()
    query.returns = with_returns
    query.positions = inline_positions
    return self.client.GetPortfolioFeed(query=query).entry

  def GetPositions(self, portfolio, with_returns=False, inline_transactions=False):
    query = PositionQuery()
    query.returns = with_returns
    query.transactions = inline_transactions
    return self.client.GetPositionFeed(portfolio, query=query).entry

  def GetTransactions(self, position=None, portfolio=None, ticker=None):
    if position:
      feed = self.client.GetTransactionFeed(position)
    elif portfolio and ticker:
      feed = self.client.GetTransactionFeed(
          portfolio_id=portfolio.portfolio_id, ticker_id=ticker)
    return feed.entry

  @ShowCallDetails
  def TestShowDetails(self, with_returns=False, inline_positions=False,
      inline_transactions=False):
    portfolios = self.GetPortfolios(with_returns, inline_positions)
    for pfl in portfolios:
      PrintPortfolio(pfl, with_returns)
      positions = self.GetPositions(pfl, with_returns, inline_transactions)
      for pos in positions:
        PrintPosition(pos, with_returns)
        PrintTransactions(self.GetTransactions(pos))

  def DeletePortfoliosByName(self, portfolio_titles):
    for pfl in self.GetPortfolios():
      if pfl.portfolio_title in portfolio_titles:
        self.client.DeletePortfolio(pfl)

  def AddPortfolio(self, portfolio_title, currency_code):
    pfl = PortfolioEntry(portfolio_data=PortfolioData(
        currency_code=currency_code))
    pfl.portfolio_title = portfolio_title
    return self.client.AddPortfolio(pfl)

  def UpdatePortfolio(self, portfolio,
      portfolio_title=None, currency_code=None):
    if portfolio_title:
      portfolio.portfolio_title = portfolio_title
    if currency_code:
      portfolio.portfolio_data.currency_code = currency_code
    return self.client.UpdatePortfolio(portfolio)

  def DeletePortfolio(self, portfolio):
    self.client.DeletePortfolio(portfolio)

  @ShowCallDetails
  def TestManagePortfolios(self):
    pfl_one = 'Portfolio Test: Emerging Markets 12345'
    pfl_two = 'Portfolio Test: Renewable Energy 31415'

    print '---- Deleting portfolios ----'
    self.DeletePortfoliosByName([pfl_one, pfl_two])
    PrintPortfolios(self.GetPortfolios())
    print '---- Adding new portfolio ----'
    pfl = self.AddPortfolio(pfl_one, 'SGD')
    PrintPortfolios(self.GetPortfolios())
    print '---- Changing portfolio title and currency code ----'
    pfl = self.UpdatePortfolio(pfl, pfl_two, 'USD')
    PrintPortfolios(self.GetPortfolios())
    print '---- Deleting portfolio ----'
    self.DeletePortfolio(pfl)
    PrintPortfolios(self.GetPortfolios())

  def Transact(self, type, portfolio, ticker, date=None, shares=None,
      notes=None, price=None, commission=None, currency_code=None):
    if price is not None:
      price = Price(money=[Money(amount=str(price),
          currency_code=currency_code or
          portfolio.portfolio_data.currency_code)])
    if commission is not None:
      commission = Commission(money=[Money(amount=str(comission),
          currency_code=currency_code or
          portfolio.portfolio_data.currency_code)])
    if date is not None and isinstance(date, datetime.datetime):
      date = date.isoformat()
    if shares is not None:
      shares = str(shares)
    txn = TransactionEntry(transaction_data=TransactionData(type=type,
        date=date, shares=shares, notes=notes, price=price,
        commission=commission))
    return self.client.AddTransaction(txn,
        portfolio_id=portfolio.portfolio_id, ticker_id=ticker)
  
  def Buy(self, portfolio, ticker, **kwargs):
    return self.Transact('Buy', portfolio, ticker, **kwargs)
  
  def Sell(self, portfolio, ticker, **kwargs):
    return self.Transact('Sell', portfolio, ticker, **kwargs)

  def GetPosition(self, portfolio, ticker, with_returns=False, inline_transactions=False):
    query = PositionQuery()
    query.returns = with_returns
    query.transactions = inline_transactions
    return self.client.GetPosition(
        portfolio_id=portfolio.portfolio_id, ticker_id=ticker, query=query)

  def DeletePosition(self, position):
    self.client.DeletePosition(position_entry=position)

  def UpdateTransaction(self, transaction):
    self.client.UpdateTransaction(transaction)

  def DeleteTransaction(self, transaction):
    self.client.DeleteTransaction(transaction)

  @ShowCallDetails
  def TestManageTransactions(self):
    pfl_title = 'Transaction Test: Technology 27182'
    self.DeletePortfoliosByName([pfl_title])

    print '---- Adding new portfolio ----'
    pfl = self.AddPortfolio(pfl_title, 'USD')
    PrintPortfolios(self.GetPortfolios())

    print '---- Adding buy transactions ----'
    tkr1 = 'NASDAQ:GOOG'
    date = datetime.datetime(2009,04,01)
    days = datetime.timedelta(1)
    txn1 = self.Buy(pfl, tkr1, shares=500, price=321.00, date=date)
    txn2 = self.Buy(pfl, tkr1, shares=150, price=312.00, date=date+15*days)
    pos = self.GetPosition(portfolio=pfl, ticker=tkr1, with_returns=True)
    PrintPosition(pos, with_returns=True)
    PrintTransactions(self.GetTransactions(pos))

    print '---- Adding sell transactions ----'
    txn3 = self.Sell(pfl, tkr1, shares=400, price=322.00, date=date+30*days)
    txn4 = self.Sell(pfl, tkr1, shares=200, price=330.00, date=date+45*days)
    pos = self.GetPosition(portfolio=pfl, ticker=tkr1, with_returns=True)
    PrintPosition(pos, with_returns=True)
    PrintTransactions(self.GetTransactions(pos))

    print "---- Modifying first and deleting third ----"
    txn1.transaction_data.shares = '400.0'
    self.UpdateTransaction(txn1)
    self.DeleteTransaction(txn3)
    pos = self.GetPosition(portfolio=pfl, ticker=tkr1, with_returns=True)
    PrintPosition(pos, with_returns=True)
    PrintTransactions(self.GetTransactions(pos))

    print "---- Deleting position ----"
    print "Number of positions (before):", len(self.GetPositions(pfl))
    self.DeletePosition(pos)
    print "Number of positions (after) :", len(self.GetPositions(pfl))

    print '---- Deleting portfolio ----'
    self.DeletePortfolio(pfl)
    PrintPortfolios(self.GetPortfolios())


if __name__ == '__main__':
  try:
    email = sys.argv[1]
    password = sys.argv[2]
    cases = sys.argv[3:]
  except IndexError:
    print "Usage: test_finance account@google.com password [0 1 2...]"
    sys.exit(1)

  tester = FinanceTester(email, password)
  tests = [
      tester.TestShowDetails,
      lambda: tester.TestShowDetails(with_returns=True),
      tester.TestManagePortfolios,
      tester.TestManageTransactions,
      lambda: tester.TestShowDetails(with_returns=True, inline_positions=True),
      lambda: tester.TestShowDetails(with_returns=True, inline_positions=True,
          inline_transactions=True),]
  if not cases:
    cases = range(len(tests))
  for i in cases:
    print "===== TEST CASE", i, "="*50
    tests[int(i)]()
