.outputs A_Accepted A_Cancelled A_Complete A_Concept A_Create_.00Application A_Denied A_Incomplete A_Pending A_Submitted A_Validating O_Accepted O_Cancelled O_Create_.00Offer O_Created O_Refused O_Returned O_Sent_.00_.04mail_.00and_.00online_.05 O_Sent_.00_.04online_.00only_.05 W_Assess_.00potential_.00fraud W_Call_.00after_.00offers W_Call_.00incomplete_.00files W_Complete_.00application W_Handle_.00leads W_Validate_.00application endevent startevent
.graph
p3 A_Accepted
A_Accepted p3
p3 A_Cancelled
A_Cancelled p3
p3 A_Complete
A_Complete p3
p0 A_Concept
p5 A_Create_.00Application
p3 A_Denied
A_Denied p3
p3 A_Incomplete
A_Incomplete p3
p1 A_Pending
p0 A_Submitted
A_Submitted p0
p3 A_Validating
A_Validating p3
p3 O_Accepted
p3 O_Cancelled
O_Cancelled p3
p3 O_Create_.00Offer
p2 O_Created
p3 O_Refused
O_Refused p3
p3 O_Returned
O_Returned p3
p3 O_Sent_.00_.04mail_.00and_.00online_.05
O_Sent_.00_.04mail_.00and_.00online_.05 p3
p3 O_Sent_.00_.04online_.00only_.05
O_Sent_.00_.04online_.00only_.05 p3
W_Assess_.00potential_.00fraud
p3 W_Call_.00after_.00offers
W_Call_.00after_.00offers p3
p3 W_Call_.00incomplete_.00files
W_Call_.00incomplete_.00files p3
p3 W_Complete_.00application
W_Complete_.00application p3
p0 W_Handle_.00leads
W_Handle_.00leads p0
p3 W_Validate_.00application
W_Validate_.00application p3
p3 endevent
p4 startevent
A_Concept p3
A_Create_.00Application p0
A_Pending p3
O_Accepted p1
O_Create_.00Offer p2
O_Created p3
startevent p5
.marking { p4 }
.end
