#!/usr/bin/python
import sys

from mercurial import ui, hg

repo = hg.repository(ui.ui(), '..')
changes = [d for d in repo[sys.argv[1]].descendants()]
for c in changes:
  description = c.description()
  if description != 'merge' and description != 'Merge':
    print description
