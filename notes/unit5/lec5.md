# Testing

Like most software, tests can only test externally observable things

ActorSystem + TestProbe is pretty handy:

p.send(actor, "messsage")
p.expectReponse("somestuff")


