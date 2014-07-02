JFLAGS =
JC = javac
.SUFFIXES: .java .class
.java.class:
	$(JC) $(JFLAGS) $*.java

CLASSES = \
	MigratableProcess.java \
	TransactionalFileInputStream.java \
	TransactionalFileOutputStream.java \
	Message.java \
	SlaveInfo.java \
	ProcessInfo.java \
	MasterThread.java \
	SlaveNode.java \
	MasterHelper.java \
	ProcessHelper.java \
	ProcessManager.java \
	GrepProcess.java \
	WebCrawlerProcss.java \
	MergesortProcess.java \
	Test.java \

default: classes

classes: $(CLASSES:.java=.class)

clean:
	$(RM) *.class