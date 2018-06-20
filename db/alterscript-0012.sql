USE `hutoma`;

/* update minp for existing bots which have the default value of 0.4 */
UPDATE `ai` SET ui_ai_confidence = 0.3 WHERE ui_ai_confidence < 0.401 AND ui_ai_confidence > 0.399; 

