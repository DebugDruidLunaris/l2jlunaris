#!/bin/bash
##########################
## Based on JTS
##########################

# Configure the database access
configure()
{
	echo "#################################################"
	echo "#				Configuration area"
	echo "#			Please answer to the questions"
	echo "#################################################"
	MYSQLDUMPPATH=`which mysqldump 2>/dev/null`
	MYSQLPATH=`which mysql 2>/dev/null`
	if [ $? -ne 0 ]; then
		echo "Unable to find MySQL binaries on your PATH"
		while :
		do
			echo -ne "\nPlease enter MySQL binaries directory (no trailing slash): "
			read MYSQLBINPATH
			if [ -e "$MYSQLBINPATH" ] && [ -d "$MYSQLBINPATH" ] && [ -e "$MYSQLBINPATH/mysqldump" ] && [ -e "$MYSQLBINPATH/mysql" ]; then
				MYSQLDUMPPATH="$MYSQLBINPATH/mysqldump"
				MYSQLPATH="$MYSQLBINPATH/mysql"
				break
			else
				echo "Invalid data. Please verify and try again."
				exit 1
			fi
		done
	fi

	# LoginServer
	echo -ne "\nEnter MySQL LOGIN SERVER hostname (default localhost): "
	read LSDBHOST
	if [ -z "$LSDBHOST" ]; then
		LSDBHOST="localhost"
	fi
	echo -ne "\nEnter MySQL LOGIN SERVER database name (default jts): "
	read LSDB
	if [ -z "$LSDB" ]; then
		LSDB="jts"
	fi
	echo -ne "\nEnter MySQL LOGIN SERVER user (default root): "
	read LSUSER
	if [ -z "$LSUSER" ]; then
		LSUSER="root"
	fi
	echo -ne "\nEnter MySQL LOGIN SERVER $LSUSER's password: "
	read LSPASS
	if [ -z "$LSPASS" ]; then
		echo "Please avoid empty password else you will have a security problem."
	fi

	# GameServer
	echo -ne "\nEnter MySQL GAME SERVER hostname (default $LSDBHOST): "
	read GSDBHOST
	if [ -z "$GSDBHOST" ]; then
		GSDBHOST="$LSDBHOST"
	fi
	echo -ne "\nEnter MySQL GAME SERVER database name (default $LSDB): "
	read GSDB
	if [ -z "$GSDB" ]; then
		GSDB="$LSDB"
	fi
	echo -ne "\nEnter MySQL GAME SERVER user (default $LSUSER): "
	read GSUSER
	if [ -z "$GSUSER" ]; then
		GSUSER="$LSUSER"
	fi
	echo -ne "\nEnter MySQL GAME SERVER $GSUSER's password: "
	read GSPASS
	if [ -z "$GSPASS" ]; then
		echo "Please avoid empty password else you will have a security problem."
	fi
}

# Actions which can be performed
action_type()
{
	echo "#################################################"
	echo "#			JTS	Database Installer Script"
	echo "#################################################"
	echo ""
	echo "What do you want to do?"
	echo "Full Installation			[f] (for first installation, this will ERASE all the existing tables)"
	echo "Installation Login DB			[l] (for installation login database, this will ERASE all the existing tables)"
	echo "Installation Game DB			[g] (for installation game database, this will ERASE all the existing tables)"
	echo "Quit this script			[q]"
	echo -ne "Choice: "
	read ACTION_CHOICE
	case "$ACTION_CHOICE" in
		"f"|"F") full_install; finish;;
		"l"|"L") login_install; finish;;
		"g"|"G") game_install; finish;;
		"q"|"Q") finish;;
		*)       action_type;;
	esac
}

# Full installation (erase and insert all tables)
full_install()
{
	echo "#################################################"
	echo "#			JTS	Full Database Installation"
	echo "#################################################"
	echo ""
	echo "[FULL DATABASE INSTALLATION]"
	login_install;
	game_install;
	echo "[COMPLETED]"
}

#Login Databese Install
login_install()
{
	echo "#################################################"
	echo "#			JTS Login Server Database Installation	"
	echo "#################################################"
	echo ""
	echo "[LOGINSERVER DATABASE]"
	loginserver_install;
	echo "[COMPLETED]"
}

#Game Databese Install
game_install()
{
	echo "#################################################"
	echo "#		JTS	Game Server Database Installation	"
	echo "#################################################"
	echo ""
	echo "[GAMESERVER DATABASE]"
	serverdata_install;
	userdata_install;
	udpate_install;
	echo "[COMPLETED]"
}


loginserver_install()
{
	for tab in \
		sql/login/accounts.sql \
		sql/login/gameservers.sql \
		sql/login/account_log.sql \
	; do
		echo Loading $tab ...
		$MYL < $tab
	done
}

serverdata_install()
{
	for tab in \
		sql/game/account_bonus.sql \
		sql/game/ally_data.sql \
		sql/game/bans.sql \
		sql/game/bbs_clannotice.sql \
		sql/game/bbs_commission.sql \
		sql/game/bbs_favorites.sql \
		sql/game/bbs_lottery.sql \
		sql/game/bbs_mail.sql \
		sql/game/bbs_memo.sql \
		sql/game/bbs_news.sql \
		sql/game/bbs_sms_country.sql \
		sql/game/bbs_sms_data.sql \
		sql/game/bbs_teleport.sql \
		sql/game/bot_report.sql \
		sql/game/bot_reported_punish.sql \
		sql/game/castle.sql \
		sql/game/castle_damage_zones.sql \
		sql/game/castle_door_upgrade.sql \
		sql/game/castle_hired_guards.sql \
		sql/game/castle_manor_procure.sql \
		sql/game/castle_manor_production.sql \
		sql/game/character_blocklist.sql \
		sql/game/character_bookmarks.sql \
		sql/game/character_effects_save.sql \
		sql/game/character_friends.sql \
		sql/game/character_group_reuse.sql \
		sql/game/character_hennas.sql \
		sql/game/character_instances.sql \
		sql/game/character_l2top_votes.sql \
		sql/game/character_macroses.sql \
		sql/game/character_minigame_score.sql \
		sql/game/character_mmotop_votes.sql \
		sql/game/character_offline_buffer_buffs.sql \
		sql/game/character_offline_buffers.sql \
		sql/game/character_post_friends.sql \
		sql/game/character_premium_items.sql \
		sql/game/character_quests.sql \
		sql/game/character_recipebook.sql \
		sql/game/character_secondary_password.sql \
		sql/game/character_shortcuts.sql \
		sql/game/character_skills.sql \
		sql/game/character_skills_save.sql \
		sql/game/character_sms_donate.sql \
		sql/game/character_subclasses.sql \
		sql/game/character_variables.sql \
		sql/game/character_vote4_votes.sql \
		sql/game/characters.sql \
		sql/game/clan_data.sql \
		sql/game/clan_privs.sql \
		sql/game/clan_skills.sql \
		sql/game/clan_subpledges.sql \
		sql/game/clan_subpledges_skills.sql \
		sql/game/clan_wars.sql \
		sql/game/clanhall.sql \
		sql/game/clanlist_service.sql \
		sql/game/class_list.sql \
		sql/game/couples.sql \
		sql/game/cursed_weapons.sql \
		sql/game/dominion.sql \
		sql/game/dominion_rewards.sql \
		sql/game/epic_boss_spawn.sql \
		sql/game/event_data.sql \
		sql/game/event_hitman.sql \
		sql/game/event_tvt_arena.sql \
		sql/game/fish.sql \
		sql/game/fishing_championship.sql \
		sql/game/fishreward.sql \
		sql/game/fortress.sql \
		sql/game/four_sepulchers_spawnlist.sql \
		sql/game/game_log.sql \
		sql/game/games.sql \
		sql/game/heroes.sql \
		sql/game/heroes_diary.sql \
		sql/game/HWID_bans.sql \
		sql/game/HWID_info.sql \
		sql/game/hwid_last.sql \
		sql/game/hwid_log.sql \
		sql/game/item_attributes.sql \
		sql/game/item_auction.sql \
		sql/game/item_auction_bid.sql \
		sql/game/items.sql \
		sql/game/items_delayed.sql \
		sql/game/lvlupgain.sql \
		sql/game/mail.sql \
		sql/game/olympiad_history.sql \
		sql/game/olympiad_nobles.sql \
		sql/game/pccafe_coupon.sql \
		sql/game/pet_data.sql \
		sql/game/petitions.sql \
		sql/game/pets.sql \
		sql/game/pets_skills.sql \
		sql/game/raidboss_points.sql \
		sql/game/raidboss_status.sql \
		sql/game/random_spawn.sql \
		sql/game/random_spawn_loc.sql \
		sql/game/recitems.sql \
		sql/game/residence_functions.sql \
		sql/game/server_variables.sql \
		sql/game/service_bonus.sql \
		sql/game/service_bonus_log.sql \
		sql/game/seven_signs.sql \
		sql/game/seven_signs_festival.sql \
		sql/game/seven_signs_status.sql \
		sql/game/siege_clans.sql \
		sql/game/siege_guards.sql \
		sql/game/siege_players.sql \
		sql/game/vote.sql \
	; do
		echo Loading $tab ...
		$MYG < $tab
	done
}


# End of the script
finish()
{
	echo ""
	echo "Script execution finished."
	exit 0
}

# Clear console
clear

# Call configure function
configure

# Open MySQL connections
MYL="$MYSQLPATH -h $LSDBHOST -u $LSUSER --password=$LSPASS -D $LSDB"
MYG="$MYSQLPATH -h $GSDBHOST -u $GSUSER --password=$GSPASS -D $GSDB"

# Ask action to do
action_type