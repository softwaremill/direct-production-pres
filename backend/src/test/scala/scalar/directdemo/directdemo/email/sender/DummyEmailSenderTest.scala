package scalar.directdemo.email.sender

import scalar.directdemo.email.EmailData
import scalar.directdemo.test.BaseTest

class DummyEmailSenderTest extends BaseTest:
  it should "send scheduled email" in {
    DummyEmailSender(EmailData("test@sml.com", "subject", "content"))
    DummyEmailSender.findSentEmail("test@sml.com", "subject").isDefined shouldBe true
  }
