.outputs S1_A_PARTLYSUBMITTED S1_A_SUBMITTED S1_W_Afhandelen_.00leads S1_W_Beoordelen_.00fraude S1_decision S2_W_Beoordelen_.00fraude S2_W_Completeren_.00aanvraag S2_decision S3_W_Beoordelen_.00fraude S3_W_Completeren_.00aanvraag S3_W_Nabellen_.00incomplete_.00dossiers S3_W_Nabellen_.00offertes S3_W_Valideren_.00aanvraag S3_decision S4_W_Beoordelen_.00fraude S4_W_Nabellen_.00incomplete_.00dossiers S4_W_Nabellen_.00offertes S4_W_Valideren_.00aanvraag S4_decision endevent startevent
.graph
p3 S1_A_PARTLYSUBMITTED
p4 S1_A_SUBMITTED
p1 S1_W_Afhandelen_.00leads
S1_W_Afhandelen_.00leads p1
p1 S1_W_Beoordelen_.00fraude
S1_W_Beoordelen_.00fraude p1
p1 S1_decision
p0 S2_W_Beoordelen_.00fraude
S2_W_Beoordelen_.00fraude p0
p0 S2_W_Completeren_.00aanvraag
S2_W_Completeren_.00aanvraag p0
p0 S2_decision
S2_decision p0
p0 S3_W_Beoordelen_.00fraude
S3_W_Beoordelen_.00fraude p0
p0 S3_W_Completeren_.00aanvraag
S3_W_Completeren_.00aanvraag p0
p0 S3_W_Nabellen_.00incomplete_.00dossiers
S3_W_Nabellen_.00incomplete_.00dossiers p0
p0 S3_W_Nabellen_.00offertes
S3_W_Nabellen_.00offertes p0
p0 S3_W_Valideren_.00aanvraag
S3_W_Valideren_.00aanvraag p0
p0 S3_decision
S3_decision p0
p0 S4_W_Beoordelen_.00fraude
S4_W_Beoordelen_.00fraude p0
p0 S4_W_Nabellen_.00incomplete_.00dossiers
S4_W_Nabellen_.00incomplete_.00dossiers p0
p0 S4_W_Nabellen_.00offertes
S4_W_Nabellen_.00offertes p0
p0 S4_W_Valideren_.00aanvraag
S4_W_Valideren_.00aanvraag p0
p0 S4_decision
S4_decision p0
p0 endevent
p2 startevent
S1_A_PARTLYSUBMITTED p1
S1_A_SUBMITTED p3
S1_decision p0
startevent p4
.marking { p2 }
.end
