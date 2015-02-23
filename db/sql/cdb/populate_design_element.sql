LOCK TABLES `design_element` WRITE;
/*!40000 ALTER TABLE `design_element` DISABLE KEYS */;
INSERT INTO `design_element` VALUES
(1,'S01A:GV1',1,NULL,NULL,1,NULL,'Upstream end of arc',1.00,210),
(2,'S01A:VC1',1,NULL,NULL,3,NULL,'Inside A:Q1',2.00,211),
(3,'S01A:BPM1',1,NULL,NULL,28,NULL,'Between A:Q1 and A:Q2',3.00,212),
(4,'S01A:VC2',1,NULL,NULL,4,NULL,'Inside A:Q2',4.00,213),
(5,'S01A:VC3',1,NULL,NULL,5,NULL,'Inside A:M1',5.00,214),
(6,'S01A:EA1',1,NULL,NULL,29,NULL,'Downstream end of S01A:VC3',6.00,215),
(7,'S01A:VC4',1,NULL,NULL,6,NULL,'Inside A:Q3',7.00,216),
(8,'S01A:BPM2',1,NULL,NULL,28,NULL,'Between A:Q3 and A:S1',8.00,217),
(9,'S01A:VC5',1,NULL,NULL,7,NULL,'Inside A:S1',9.00,218),
(10,'S01A:VC6',1,NULL,NULL,8,NULL,'Between A:S1 and A:Q4',10.00,219),
(11,'S01A:CA1',1,NULL,NULL,33,NULL,'Mounted to outboard side of S01A:VC6',11.00,220),
(12,'S01A:IP1',1,NULL,NULL,36,NULL,'Mounted to bottom of S01A:VC6',12.00,221),
(13,'S01A:VGP1',1,NULL,NULL,35,NULL,'Mounted to top of S01A:VC6',13.00,222),
(14,'S01A:VC7',1,NULL,NULL,9,NULL,'Inside A:Q4',14.00,223),
(15,'S01A:BPM3',1,NULL,NULL,28,NULL,'Between A:Q4 and A:S2',15.00,224),
(16,'S01A:VC8',1,NULL,NULL,10,NULL,'Inside A:S2',16.00,225),
(17,'S01A:BPM4',1,NULL,NULL,28,NULL,'Between A:Q5 and A:S3',20.00,226),
(18,'S01A:VC9',1,NULL,NULL,11,NULL,'Between A:S2 and A:Q5',18.00,227),
(19,'S01A:BPM5',1,NULL,NULL,28,NULL,'Between A:S3 and A:Q6',19.00,228),
(20,'S01A:VC10',1,NULL,NULL,12,NULL,'Inside A:Q5',20.00,229),
(21,'S01A:VC11',1,NULL,NULL,13,NULL,'Inside A:S3',21.00,230),
(22,'S01A:EA2',1,NULL,NULL,30,NULL,'Downstream end of S01A:VC3',22.00,231),
(23,'S01A:VC12',1,NULL,NULL,14,NULL,'Inside A:Q6',23.00,232),
(24,'S01A:GV2',1,NULL,NULL,1,NULL,'Between A:Q7 and A:M3',25.00,233),
(25,'S01A:VC13',1,NULL,NULL,15,NULL,'Inside A:M2',26.00,234),
(26,'S01A:BPM6',1,NULL,NULL,28,NULL,'Between A:M3 and A:Q8',27.00,235),
(27,'S01A:VC14',1,NULL,NULL,16,NULL,'Inside A:Q8',28.00,236),
(28,'S01A:VC15',1,NULL,NULL,8,NULL,'Between A:Q8 and A:M4',30.00,237),
(29,'S01A:IP2',1,NULL,NULL,36,NULL,'Mounted to bottom of S01A:VC15',31.00,238),
(30,'S01A:VGP2',1,NULL,NULL,35,NULL,'Mounted to top of S01A:VC15',32.00,239),
(31,'S01A:VC16',1,NULL,NULL,17,NULL,'Inside A:M4 and B:Q8',33.00,240),
(32,'S01A:VC17',1,NULL,NULL,24,NULL,'Between S01A:VC6 and S01A:VC11',34.00,241),
(33,'S01A:VC18',1,NULL,NULL,25,NULL,'Between S01A:VC11 and ID front end',35.00,242),
(34,'S01B:BPM6',1,NULL,NULL,28,NULL,'Between B:Q8 and B:M3',36.00,243),
(35,'S01B:VC13',1,NULL,NULL,18,NULL,'Inside B:M3 and B:Q7',37.00,244),
(36,'S01B:TA2',1,NULL,NULL,84,NULL,'Immediately upstream of S01B:GV2 ',38.00,245),
(37,'S01B:GV2',1,NULL,NULL,2,NULL,'Between B:M3 and Q:Q8',39.00,246),
(38,'S01B:VC12',1,NULL,NULL,19,NULL,'Inside B:Q7',40.00,247),
(39,'S01B:VC11',1,NULL,NULL,20,NULL,'Inside B:M2',41.00,248),
(40,'S01B:CA1',1,NULL,NULL,33,NULL,'Downstream end of S01B:VC11',42.00,249),
(41,'S01B:WA2',1,NULL,NULL,31,NULL,'Downstream end of S01B:VC11',43.00,250),
(42,'S01B:VC10',1,NULL,NULL,12,NULL,'Inside B:Q6',44.00,251),
(43,'S01B:BPM5',1,NULL,NULL,28,NULL,'Between B:Q6 and B:S3',45.00,252),
(44,'S01B:VC9',1,NULL,NULL,11,NULL,'Inside B:S3',46.00,253),
(45,'S01B:BPM4',1,NULL,NULL,28,NULL,'Between B:S3 and B:Q5',47.00,254),
(46,'S01B:VC8',1,NULL,NULL,10,NULL,'Inside B:Q5 and B:S2',48.00,255),
(47,'S01B:BPM3',1,NULL,NULL,28,NULL,'Between B:S2 and B:Q4',49.00,256),
(48,'S01B:VC7',1,NULL,NULL,9,NULL,'Inside B:Q4',50.00,257),
(49,'S01B:TA1',1,NULL,NULL,84,NULL,'Immediately upstream of S01B:VC6 ',51.00,258),
(50,'S01B:VC6',1,NULL,NULL,8,NULL,'Between B:Q4 and B:S1',52.00,259),
(51,'S01B:IP1',1,NULL,NULL,36,NULL,'Mounted to bottom of S01B:VC6',53.00,260),
(52,'S01B:VGP1',1,NULL,NULL,35,NULL,'Mounted to top of S01B:VC6',54.00,261),
(53,'S01B:RGA1',1,NULL,NULL,37,NULL,'Mounted to outboard side of S01B:VC6',55.00,262),
(54,'S01B:VC5',1,NULL,NULL,21,NULL,'Inside B:S1',56.00,263),
(55,'S01B:BPM2',1,NULL,NULL,28,NULL,'Between B:S1 and B:Q3',57.00,264),
(56,'S01B:VC4',1,NULL,NULL,22,NULL,'Inside B:Q3',58.00,265),
(57,'S01B:VC3',1,NULL,NULL,23,NULL,'Inside B:M1',59.00,266),
(58,'S01B:WA1',1,NULL,NULL,32,NULL,'Downstream end of S01B:VC3',60.00,267),
(59,'S01B:VC2',1,NULL,NULL,4,NULL,'Inside B:Q2',61.00,268),
(60,'S01B:BPM1',1,NULL,NULL,28,NULL,'Between B:Q2 and B:Q1',62.00,269),
(61,'S01B:VC1',1,NULL,NULL,3,NULL,'Inside B:Q1',63.00,270),
(62,'S01B:GV1',1,NULL,NULL,1,NULL,'Downstream end of arc',64.00,271),
(63,'S01B:VC14',1,NULL,NULL,26,NULL,'Between S01B:VC11 and S01B:VC6',65.00,272),
(64,'S01B:VC15',1,NULL,NULL,27,NULL,'Between S01B:VC6 and BM front end',66.00,273),
(65,'S01A:IPCTL1',1,NULL,NULL,38,NULL,'Storage ring mezzanine rack #',67.00,274),
(66,'S01B:IPCTL1',1,NULL,NULL,38,NULL,'Storage ring mezzanine rack #',68.00,275),
(67,'S01A:IPCBL1',1,NULL,NULL,39,NULL,'HV cable tray: S01A:IP1 to S01A:IPCTL1',69.00,276),
(68,'S01A:IPCBL2',1,NULL,NULL,39,NULL,'HV cable tray: S01A:IP2 to S01A:IPCTL1',70.00,277),
(69,'S01B:IPCBL1',1,NULL,NULL,39,NULL,'HV cable tray: S01B:IP1 to S01B:IPCTL1',71.00,278),
(70,'S01A:VGCTL1',1,NULL,NULL,40,NULL,'Storage ring mezzanine rack #',72.00,279),
(71,'S01B:VGCTL1',1,NULL,NULL,40,NULL,'Storage ring mezzanine rack #',73.00,280),
(72,'S01A:VGCBL1',1,NULL,NULL,41,NULL,'Signal cable tray: S01A:VGP1 to S01A:VGCTL1',74.00,281),
(73,'S01A:VGCBL2',1,NULL,NULL,42,NULL,'Signal cable tray: S01A:VGP1 to S01A:VGCTL1',75.00,282),
(74,'S01A:VGCBL3',1,NULL,NULL,41,NULL,'Signal cable tray: S01A:VGP2 to S01A:VGCTL1',76.00,283),
(75,'S01A:VGCBL4',1,NULL,NULL,42,NULL,'Signal cable tray: S01A:VGP2 to S01A:VGCTL1',77.00,284),
(76,'S01B:VGCBL1',1,NULL,NULL,41,NULL,'Signal cable tray: S01B:VGP1 to S01A:VGCTL2',78.00,285),
(77,'S01B:VGCBL2',1,NULL,NULL,42,NULL,'Signal cable tray: S01B:VGP1 to S01B:VGCTL2',79.00,286),
(78,'S01B:RGACBL',1,NULL,NULL,43,NULL,'Signal cable tray: S01B:VGP1 to TBD',80.00,287),
(79,'S01A:GVCBL1',1,NULL,NULL,45,NULL,'Signal cable tray: S01A:GV1 to S01A:GVCTL1',85.00,288),
(80,'S01A:GVCBL2',1,NULL,NULL,45,NULL,'Signal cable tray: S01B:GV1 to S01B:GVCTL1',86.00,289),
(81,'S01A:DIWCTL1',1,NULL,NULL,46,NULL,'Storage ring mezzanine rack #',87.00,290),
(82,'S01B:DIWCTL1',1,NULL,NULL,46,NULL,'Storage ring mezzanine rack #',88.00,291),
(83,'S01A:HTRCTL',1,NULL,NULL,47,NULL,'Storage ring mezzanine rack #',89.00,292),
(84,'S01B:HTRCTL',1,NULL,NULL,47,NULL,'Storage ring mezzanine rack #',90.00,293),
(85,'S01A:HTRCBL',1,NULL,NULL,48,NULL,'Signal cable tray: S01A:HTRCTL to S01A:VCX',91.00,294),
(86,'S01B:HTRCBL',1,NULL,NULL,48,NULL,'Signal cable tray: S01B:HTRCTL to S01B:VCX',92.00,295),
(87,'Crate 1',2,NULL,NULL,58,NULL,'',NULL,296),
(88,'IOCLIC2',2,NULL,NULL,107,NULL,'',NULL,297),
(89,'ADC1',2,NULL,NULL,89,NULL,'',NULL,298),
(90,'ADC2',2,NULL,NULL,89,NULL,'',NULL,299),
(91,'ADC3',2,NULL,NULL,89,NULL,'',NULL,300),
(92,'DAC1',2,NULL,NULL,90,NULL,'',NULL,301),
(93,'CM1',2,NULL,NULL,139,NULL,'',NULL,302),
(94,'CM2',2,NULL,NULL,139,NULL,'',NULL,303),
(95,'CM3',2,NULL,NULL,139,NULL,'',NULL,304),
(96,'Reflective Memory 1',5,NULL,NULL,158,NULL,'Reflective Memory',5.00,305),
(97,'CPU1',5,NULL,NULL,159,NULL,'CPU',2.00,306),
(98,'Chassis 1',5,NULL,NULL,156,NULL,'',1.00,307),
(99,'EVR1',5,NULL,NULL,157,NULL,'Event Receiver',3.00,308),
(100,'ctlsdaqdev2',6,NULL,NULL,108,NULL,'',NULL,309),
(101,'LLRF4 Controller #1',6,NULL,NULL,88,NULL,'',NULL,310),
(102,'ctlsdaqdev1',7,NULL,NULL,108,NULL,'',NULL,311),
(103,'SPEC Board',7,NULL,NULL,87,NULL,'PCIe board from CERN',NULL,312),
(104,'ctlsdaqsrv1',4,NULL,NULL,108,NULL,'workstation for DAQ services/storage',NULL,313),
(105,'Ctlr1',16,NULL,NULL,38,NULL,'',NULL,314),
(106,'Cable1',16,NULL,NULL,39,NULL,'',NULL,315),
(107,'Pump1',16,NULL,NULL,36,NULL,'',NULL,316),
(108,'BPM1',16,NULL,NULL,28,NULL,'',NULL,317),
(109,'BPM2',16,NULL,NULL,28,NULL,'',NULL,318),
(110,'BPM3',16,NULL,NULL,28,NULL,'',NULL,319),
(111,'Chamber6',16,NULL,NULL,8,NULL,'',NULL,320),
(112,'Chamber4',16,NULL,NULL,6,NULL,'',NULL,321),
(113,'Chamber5',16,NULL,NULL,7,NULL,'',NULL,322),
(114,'Chamber8',16,NULL,NULL,10,NULL,'',NULL,323),
(115,'Chamber7',16,NULL,NULL,9,NULL,'',NULL,324),
(116,'Gauge1',16,NULL,NULL,35,NULL,'',NULL,325),
(117,'Cable2',16,NULL,NULL,42,NULL,'',NULL,326),
(118,'Ctlr2',16,NULL,NULL,40,NULL,'',NULL,327),
(119,'Cable3',16,NULL,NULL,41,NULL,'',NULL,328),
(127,'A:Q1',19,NULL,NULL,169,NULL,'',1.00,336),
(128,'A:Q2',19,NULL,NULL,171,NULL,'',2.00,337),
(129,'B:Q2',20,NULL,NULL,171,NULL,'',1.00,338),
(130,'B:Q1',20,NULL,NULL,169,NULL,'',2.00,339),
(131,'A:Q6',22,NULL,NULL,171,NULL,'',7.00,340),
(132,'A:Q3',22,NULL,NULL,172,NULL,'',1.00,341),
(133,'A:Q5',22,NULL,NULL,172,NULL,'',5.00,342),
(134,'A:Q4',22,NULL,NULL,171,NULL,'',3.00,343),
(135,'A:S1',22,NULL,NULL,173,NULL,'',2.00,344),
(136,'A:S2',22,NULL,NULL,174,NULL,'',4.00,345),
(137,'A:S3',22,NULL,NULL,173,NULL,'',6.00,346),
(138,'B:Q5',23,NULL,NULL,172,NULL,'',3.00,347),
(139,'B:Q6',23,NULL,NULL,171,NULL,'',1.00,348),
(140,'B:Q4',23,NULL,NULL,171,NULL,'',5.00,349),
(141,'B:S2',23,NULL,NULL,174,NULL,'',4.00,350),
(142,'B:Q3',23,NULL,NULL,172,NULL,'',7.00,351),
(143,'B:S1',23,NULL,NULL,173,NULL,'',6.00,352),
(144,'B:S3',23,NULL,NULL,173,NULL,'',2.00,353),
(145,'A:Q8',24,NULL,NULL,171,NULL,'',3.00,354),
(146,'B:Q8',24,NULL,NULL,171,NULL,'',5.00,355),
(147,'A:Q7',24,NULL,NULL,169,NULL,'',1.00,356),
(148,'B:Q7',24,NULL,NULL,169,NULL,'',7.00,357),
(149,'A:M3',24,NULL,NULL,175,NULL,'',2.00,358),
(150,'B:M3',24,NULL,NULL,175,NULL,'',6.00,359),
(151,'A:M4',24,NULL,NULL,175,NULL,'',4.00,360),
(152,'S01A:P1',28,NULL,NULL,179,NULL,'',1.00,361),
(153,'S01A:Q1',28,NULL,NULL,176,NULL,'',2.00,362),
(154,'S01A:Q2',28,NULL,NULL,176,NULL,'',3.00,363),
(155,'S01A:S1',28,NULL,NULL,177,NULL,'',4.00,364),
(156,'RTFB DAQ',4,5,NULL,NULL,NULL,'',1.00,365),
(157,'SR RF DAQ',4,6,NULL,NULL,NULL,'',2.00,366),
(158,'DSC DAQ',4,7,NULL,NULL,NULL,'',3.00,367),
(159,'DMM Water Handling System',13,14,NULL,NULL,NULL,'',1.00,368),
(160,'DMM Magnets',13,15,NULL,NULL,NULL,'',2.00,369),
(161,'DMM Vacuum Components',13,16,NULL,NULL,NULL,'',3.00,370),
(162,'DMM Supports',13,17,NULL,NULL,NULL,'',4.00,371),
(164,'Girder 2',21,25,NULL,NULL,NULL,'A:M1',2.00,373),
(165,'Girder 4',21,25,NULL,NULL,NULL,'A:M2',4.00,374),
(166,'Girder 3',21,22,NULL,NULL,NULL,'A:Q3, A:S1, A:Q4, A:S2, A:Q5, A:S3, A:Q6',3.00,375),
(167,'Girder 8',21,25,NULL,NULL,NULL,'B:M1',8.00,376),
(168,'Girder 6',21,25,NULL,NULL,NULL,'B:M2',6.00,377),
(169,'Girder 9',21,20,NULL,NULL,NULL,'B:Q2, B:Q1',9.00,378),
(170,'Girder 7',21,22,NULL,NULL,NULL,'B:Q6, B:S3, B:Q5, B:S2, B:Q4, B:S1, B:Q3',7.00,379),
(171,'Girder 5',21,24,NULL,NULL,NULL,'A:Q7, A:M3, A:Q8, A:M4, B:Q8, B:M3, B:Q7',5.00,380),
(172,'Girder 1',21,19,NULL,NULL,NULL,'A:Q1, A:Q2',1.00,381),
(173,'Q6a',15,NULL,NULL,166,NULL,NULL,1.00,420),
(174,'Q6b',15,NULL,NULL,166,NULL,NULL,2.00,419),
(175,'Q6d',15,NULL,NULL,166,NULL,NULL,5.00,424),
(176,'S1a',15,NULL,NULL,167,NULL,NULL,4.00,422),
(177,'Q6c',15,NULL,NULL,166,NULL,NULL,3.00,423),
(179,'Base',17,NULL,NULL,168,NULL,NULL,NULL,425),
(180,'Plinth',17,NULL,NULL,187,NULL,'',NULL,426),
(183,'AQ1',29,NULL,NULL,176,NULL,'',NULL,431),
(184,'AQ2',29,NULL,NULL,176,NULL,'',NULL,432);
/*!40000 ALTER TABLE `design_element` ENABLE KEYS */;
UNLOCK TABLES;
