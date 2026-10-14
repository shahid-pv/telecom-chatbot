INSERT INTO plans VALUES
(1,'Daily Data Pack','PREPAID',19,1,1.00,'1 GB high-speed data for 1 day, calls and SMS not included.'),
(2,'Starter 28','PREPAID',199,28,2.00,'2 GB per day, unlimited local calls, 100 SMS per day.'),
(3,'Value 28','PREPAID',299,28,3.00,'3 GB per day, unlimited local calls, 100 SMS per day.'),
(4,'Flex 56','PREPAID',499,56,2.00,'2 GB per day, unlimited local calls, 100 SMS per day.'),
(5,'Max 84','PREPAID',799,84,2.50,'2.5 GB per day, unlimited local calls, 100 SMS per day.'),
(6,'Basic Postpaid','POSTPAID',399,30,50.00,'50 GB monthly data, unlimited local calls, 100 SMS per day.'),
(7,'Plus Postpaid','POSTPAID',599,30,100.00,'100 GB monthly data, unlimited local calls, 100 SMS per day.'),
(8,'Talk 28','PREPAID',149,28,1.00,'1 GB per day, unlimited local calls, 100 SMS per day.');
INSERT INTO customers VALUES
(1,'Asha Demo','+910000000001','asha@example.demo',2,87.00,CURRENT_DATE+25,1840,'ACTIVE'),
(2,'Ravi Demo','+910000000002','ravi@example.demo',1,8.00,CURRENT_DATE+1,720,'ACTIVE'),
(3,'Mina Demo','+910000000003','mina@example.demo',3,142.00,CURRENT_DATE+2,4210,'ACTIVE'),
(4,'Dev Demo','+910000000004','dev@example.demo',4,53.00,CURRENT_DATE+45,9700,'ACTIVE');
INSERT INTO transactions VALUES
(1,1,'RECHARGE',199,'Starter 28 recharge',now()-interval '20 days'),(2,1,'ADDON_DEDUCTION',19,'Daily Data Pack 1 GB',now()-interval '1 day'),(3,1,'RECHARGE',100,'Top-up',now()-interval '12 days'),(4,1,'ADDON_DEDUCTION',19,'Daily Data Pack 1 GB',now()-interval '8 days'),(5,1,'REFUND',19,'Failed add-on reversal',now()-interval '7 days'),(6,1,'ADDON_DEDUCTION',29,'Extra data pack',now()-interval '5 days'),(7,1,'RECHARGE',50,'Top-up',now()-interval '3 days'),(8,1,'PLAN_PURCHASE',199,'Starter 28 plan purchase',now()-interval '20 days'),
(9,2,'RECHARGE',50,'Top-up',now()-interval '14 days'),(10,2,'PLAN_PURCHASE',19,'Daily Data Pack',now()-interval '6 days'),(11,2,'ADDON_DEDUCTION',19,'Daily Data Pack 1 GB',now()-interval '4 days'),(12,2,'RECHARGE',30,'Top-up',now()-interval '3 days'),(13,2,'ADDON_DEDUCTION',19,'Daily Data Pack 1 GB',now()-interval '2 days'),(14,2,'RECHARGE',20,'Top-up',now()-interval '1 day'),(15,2,'ADDON_DEDUCTION',19,'Daily Data Pack 1 GB',now()-interval '1 hour'),
(16,3,'RECHARGE',299,'Value 28 recharge',now()-interval '26 days'),(17,3,'PLAN_PURCHASE',299,'Value 28 plan purchase',now()-interval '26 days'),(18,3,'ADDON_DEDUCTION',29,'Extra data pack',now()-interval '19 days'),(19,3,'RECHARGE',100,'Top-up',now()-interval '14 days'),(20,3,'ADDON_DEDUCTION',19,'Daily Data Pack',now()-interval '11 days'),(21,3,'REFUND',29,'Duplicate deduction reversed',now()-interval '9 days'),(22,3,'ADDON_DEDUCTION',29,'Extra data pack',now()-interval '3 days'),
(23,4,'RECHARGE',499,'Flex 56 recharge',now()-interval '35 days'),(24,4,'PLAN_PURCHASE',499,'Flex 56 plan purchase',now()-interval '35 days'),(25,4,'ADDON_DEDUCTION',19,'Daily Data Pack',now()-interval '29 days'),(26,4,'RECHARGE',100,'Top-up',now()-interval '21 days'),(27,4,'ADDON_DEDUCTION',39,'Roaming pack',now()-interval '15 days'),(28,4,'REFUND',39,'Roaming pack cancellation reversal',now()-interval '14 days'),(29,4,'ADDON_DEDUCTION',19,'Daily Data Pack',now()-interval '7 days'),(30,4,'RECHARGE',50,'Top-up',now()-interval '2 days');
INSERT INTO active_services VALUES (1,4,'Caller Tune',30,'ACTIVE',now()-interval '15 days'),(2,1,'Caller Tune',30,'INACTIVE',now()-interval '60 days');
