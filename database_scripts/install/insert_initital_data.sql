
USE proxl;


--  INSERT INITIAL VALUES USED BY WEBAPP TO HANDLE PERMISSIONS

INSERT INTO xl_user_access_level_label_description (xl_user_access_level_numeric_value, label, description) 
	VALUES ( 0, 'admin', 'application wide, admin' );
	
INSERT INTO xl_user_access_level_label_description (xl_user_access_level_numeric_value, label, description) 
	VALUES ( 25, 'create new project', 'application wide, can create new project' );

INSERT INTO xl_user_access_level_label_description (xl_user_access_level_numeric_value, label, description) 
	VALUES ( 30, 'project owner', 'control all aspects of project' );

INSERT INTO xl_user_access_level_label_description (xl_user_access_level_numeric_value, label, description) 
	VALUES ( 38, 'assistant project owner', 'change most aspects of project except add/remove other assistant project owners' );

INSERT INTO xl_user_access_level_label_description (xl_user_access_level_numeric_value, label, description) 
	VALUES ( 40, 'update project and delete runs', 'update project and delete runs' );

INSERT INTO xl_user_access_level_label_description (xl_user_access_level_numeric_value, label, description) 
	VALUES ( 50, 'update project but not delete runs', 'update project but not delete runs' );

INSERT INTO xl_user_access_level_label_description (xl_user_access_level_numeric_value, label, description) 
	VALUES ( 99, 'read project', 'not able to make changes to project' );

INSERT INTO xl_user_access_level_label_description (xl_user_access_level_numeric_value, label, description) 
	VALUES ( 9999, 'no access', 'at project level, no access to that project, at application wide level, no access to any project' );

	
--  INSERT SUPPORTED CROSS-LINKERS

INSERT INTO linker(abbr,name)VALUES( 'dss','disuccinimidyl suberate' );
INSERT INTO linker(abbr,name)VALUES( 'dsg','disuccinimidyl glutarate' );
INSERT INTO linker(abbr,name)VALUES( 'bs3','bis[sulfosuccinimidyl] suberate' );
INSERT INTO linker(abbr,name)VALUES( 'bs2','bis(sulfosuccinimidyl) glutarate' );
INSERT INTO linker(abbr,name)VALUES( 'edc','1-ethyl-3-(3-dimethylaminopropyl)carbodiimide hydrochloride' );


--  This will create an initial user in the "proxl" database
--  The initial user will have user id "initial_proxl_user"
--  The initial user will have password "FJS483792nzmv,xc4#&@(!VMKSDL"

INSERT INTO auth_user (username, password_hashed, email, user_access_level, enabled) 
VALUES ('initial_proxl_user', 'CBE805BE949A46C0E906266DD23899733A8766A059256B2A7C1174FBE29D0BBD', 'NONE', '0', '1');

INSERT INTO xl_user (auth_user_id, first_name, last_name, organization) 
VALUES ((SELECT id FROM auth_user WHERE username = 'initial_proxl_user'), 'Initial', 'User', 'NONE');