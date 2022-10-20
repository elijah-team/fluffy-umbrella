##find ../elijah-lang-master -type f | grep -v Lexer | grep -v .m2 | grep -v jar | grep -v class |grep -v antlr |  xargs pr -f -l 80 -c | nl > ../elijah-lang-`date +%b%d`.txt
#find ../elijah-lang -type f | grep -v .idea | grep -v .git | grep -v Lexer | grep -v jar | grep -v zip | grep -v .m2 | grep -v class | grep -v antlr > list
find ../elijah-lang/src ../elijah-lang//test -type f | grep -v .idea | grep -v .class | grep -v Lexer | grep -v Parser | grep -v jar | grep -v zip | grep -v .m2 | grep -v antlr > list
cat list |  xargs md5sum >> ../elijah-lang-`date +%b%d-%H%M`.txt
echo =========*=========*=========*=========*=========*=========*=========*=========*=========*=========* >> ../elijah-lang-`date +%b%d-%H%M`.txt
cat list |  xargs pr -f -l 80 | nl >> ../elijah-lang-`date +%b%d-%H%M`.txt
rm list
