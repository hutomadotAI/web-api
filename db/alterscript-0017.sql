USE hutoma;

ALTER TABLE `hutoma`.`feature_toggle` 
ADD COLUMN `id` INT NOT NULL AUTO_INCREMENT FIRST,
ADD UNIQUE INDEX `DEV_AI_FEAT_UNIQUE` (`devid` ASC, `aiid` ASC, `feature` ASC),
ADD PRIMARY KEY (`id`);
