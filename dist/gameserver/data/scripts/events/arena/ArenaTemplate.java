package events.arena;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import jts.commons.threading.RunnableImpl;
import jts.gameserver.Config;
import jts.gameserver.ThreadPoolManager;
import jts.gameserver.cache.Msg;
import jts.gameserver.listener.zone.OnZoneEnterLeaveListener;
import jts.gameserver.model.Creature;
import jts.gameserver.model.GameObjectsStorage;
import jts.gameserver.model.Player;
import jts.gameserver.model.Skill;
import jts.gameserver.model.Zone;
import jts.gameserver.model.base.Experience;
import jts.gameserver.model.base.TeamType;
import jts.gameserver.network.serverpackets.Say2;
import jts.gameserver.network.serverpackets.components.ChatType;
import jts.gameserver.scripts.Functions;
import jts.gameserver.tables.SkillTable;
import jts.gameserver.utils.Location;
import jts.gameserver.utils.PositionUtils;

public abstract class ArenaTemplate extends Functions
{
	// Essas variáveis ​​são definidas automaticamente quando o script é
	protected int _managerId;
	protected String _className;
	protected String _chatName;
	protected Long _creatorId;
	protected int _status = 0;
	protected int _battleType = 1;
	protected int _team1exp = 0;
	protected int _team2exp = 0;
	protected int _price = 10000;
	protected int _team1count = 1;
	protected int _team2count = 1;
	protected int _team1min = 1;
	protected int _team1max = 85;
	protected int _team2min = 1;
	protected int _team2max = 85;
	protected int _timeToStart = 10;
	protected boolean _timeOutTask;

	protected List<Location> _team1points;
	protected List<Location> _team2points;

	protected List<Long> _team1list;
	protected List<Long> _team2list;
	protected List<Long> _team1live;
	protected List<Long> _team2live;

	protected Map<Integer, Integer> _expToReturn;
	protected Map<Integer, Integer> _classToReturn;

	protected Zone _zone;
	protected ZoneListener _zoneListener;

	protected abstract void onLoad();

	protected abstract void onReload();

	public void template_stop()
	{
		say("Luta interrompida por razões técnicas, as taxas retornaram");
		if(_battleType == 1)
			returnAdenaToTeams();
		else if(_battleType == 2)
			returnExpToTeams();
		unParalyzeTeams();
		clearTeams();
		_status = 0;
		_timeOutTask = false;
	}

	public void template_create1(Player player)
	{
		if(_status > 0)
			show("Espere até o final da batalha", player);
		else
			show("scripts/events/arena/" + _managerId + "-1.htm", player);
	}

	public void template_create2(Player player)
	{
		if(_status > 0)
			show("Espere até o final da batalha", player);
		else
			show("scripts/events/arena/" + _managerId + "-2.htm", player);
	}

	public void template_register(Player player)
	{
		if(_status > 1)
			show("Espere até o final da batalha", player);
		else
			show("scripts/events/arena/" + _managerId + "-3.htm", player);
	}

	public void template_check1(Player player, String[] var)
	{
		if(player.isDead())
			return;

		if(var.length != 8)
		{
			show("dados inválido", player);
			return;
		}

		if(_status > 0)
		{
			show("Espere até o final da batalha", player);
			return;
		}

		try
		{
			_price = Integer.valueOf(var[0]);
			_team1count = Integer.valueOf(var[1]);
			_team2count = Integer.valueOf(var[2]);
			_team1min = Integer.valueOf(var[3]);
			_team1max = Integer.valueOf(var[4]);
			_team2min = Integer.valueOf(var[5]);
			_team2max = Integer.valueOf(var[6]);
			_timeToStart = Integer.valueOf(var[7]);
		}
		catch(Exception e)
		{
			show("dados inválido", player);
			return;
		}
		if(_price < 10000 || _price > 100000000)
		{
			show("taxa incorreta", player);
			return;
		}
		if(_team1count < 1 || _team1count > 5 || _team2count < 1 || _team2count > 5)
		{
			show("Tamanho incorreto da equipe", player);
			return;
		}
		if(_team1min < 1 || _team1min > 85 || _team2min < 1 || _team2min > 85 || _team1max < 1 || _team1max > 85 || _team2max < 1 || _team2max > 85 || _team1min > _team1max || _team2min > _team2max)
		{
			show("nível incorreto", player);
			return;
		}
		if(player.getLevel() < _team1min || player.getLevel() > _team1max)
		{
			show("nível incorreto", player);
			return;
		}
		if(_timeToStart < 1 || _timeToStart > 10)
		{
			show("momento errado", player);
			return;
		}
		if(player.getAdena() < _price)
		{
			player.sendPacket(Msg.YOU_DO_NOT_HAVE_ENOUGH_ADENA);
			return;
		}
		_battleType = 1;
		_creatorId = player.getStoredId();
		player.reduceAdena(_price, true);
		_status = 1;
		_team1list.clear();
		_team2list.clear();
		_team1live.clear();
		_team2live.clear();
		_team1list.add(player.getStoredId());
		say(player.getName() + " combate criado " + _team1count + "х" + _team2count + ", " + _team1min + "-" + _team1max + "lv vs " + _team2min + "-" + _team2max + "lv, taxa " + _price + "e, através do início de " + _timeToStart + " minutos");
		executeTask("events.arena." + _className, "announce", new Object[0], 60000);
	}

	public void template_check2(Player player, String[] var)
	{
		if(!Config.ALT_ARENA_EXP)
		{
			show("Esta opção não está disponível", player);
			return;
		}

		if(player.isDead())
			return;

		if(var.length != 7)
		{
			show("Dados incorretos", player);
			return;
		}

		if(_status > 0)
		{
			show("Espere até o final da batalha", player);
			return;
		}

		try
		{
			_team1count = Integer.valueOf(var[0]);
			_team2count = Integer.valueOf(var[1]);
			_team1min = Integer.valueOf(var[2]);
			_team1max = Integer.valueOf(var[3]);
			_team2min = Integer.valueOf(var[4]);
			_team2max = Integer.valueOf(var[5]);
			_timeToStart = Integer.valueOf(var[6]);
		}
		catch(Exception e)
		{
			show("Dados incorretos", player);
			return;
		}
		if(_team1count < 1 || _team1count > 5 || _team2count < 1 || _team2count > 5)
		{
			show("Tamanho incorreto da equipe", player);
			return;
		}
		if(_team1min < 1 || _team1min > 82 || _team2min < 1 || _team2min > 82 || _team1max < 1 || _team1max > 82 || _team2max < 1 || _team2max > 82 || _team1min > _team1max || _team2min > _team2max)
		{
			show("nível incorreto", player);
			return;
		}
		if(player.getLevel() - _team1min > 10 || _team1max - player.getLevel() > 10 || player.getLevel() - _team2min > 10 || _team2max - player.getLevel() > 10)
		{
			show("A diferença de níveis nunca pode ser mais do que 10", player);
			return;
		}
		if(player.getLevel() < _team1min || player.getLevel() > _team1max)
		{
			show("nível incorreto", player);
			return;
		}
		if(_timeToStart < 1 || _timeToStart > 10)
		{
			show("momento errado", player);
			return;
		}

		_battleType = 2;
		_creatorId = player.getStoredId();
		_team1exp = 0;
		_team2exp = 0;
		_expToReturn.clear();
		_classToReturn.clear();

		removeExp(player, 1);

		_status = 1;
		_team1list.clear();
		_team2list.clear();
		_team1live.clear();
		_team2live.clear();
		_team1list.add(player.getStoredId());
		say(player.getName() + " combate criado " + _team1count + "х" + _team2count + ", " + _team1min + "-" + _team1max + "lv vs " + _team2min + "-" + _team2max + "lv, taxa " + "experiência começa em " + _timeToStart + " min");
		executeTask("events.arena." + _className, "announce", new Object[0], 60000);
	}

	public void template_register_check(Player player, String[] var)
	{
		if(player.isDead())
			return;

		if(_status > 1)
		{
			show("Espere até o final da batalha", player);
			return;
		}

		if(var.length != 1)
		{
			show("Dados incorretos", player);
			return;
		}

		int _regTeam;
		try
		{
			_regTeam = Integer.valueOf(var[0]);
		}
		catch(Exception e)
		{
			show("Dados incorretos", player);
			return;
		}

		if(_regTeam != 1 && _regTeam != 2)
		{
			show("Número incorreto de comando, use 1 ou 2", player);
			return;
		}
		if(_team1list.contains(player.getStoredId()) || _team2list.contains(player.getStoredId()))
		{
			show("Você já está registrado", player);
			return;
		}
		if(_battleType == 1 && player.getAdena() < _price)
		{
			player.sendPacket(Msg.YOU_DO_NOT_HAVE_ENOUGH_ADENA);
			return;
		}
		if(_regTeam == 1)
		{
			if(player.getLevel() < _team1min || player.getLevel() > _team1max)
			{
				show("You do not approach the level of", player);
				return;
			}
			if(_team1list.size() >= _team1count)
			{
				show("Equipe 1 completa", player);
				return;
			}

			if(_battleType == 1)
				player.reduceAdena(_price, true);
			else if(_battleType == 2)
				removeExp(player, 1);

			_team1list.add(player.getStoredId());
			say(player.getName() + " registrado para uma equipe");
			if(_team1list.size() >= _team1count && _team2list.size() >= _team2count)
			{
				say("As equipes estão prontas, comece após 1 minuto.");
				_timeToStart = 1;
			}
		}
		else
		{
			if(player.getLevel() < _team2min || player.getLevel() > _team2max)
			{
				show("You do not approach the level of", player);
				return;
			}
			if(_team2list.size() >= _team2count)
			{
				show("Equipe 2 completa", player);
				return;
			}

			if(_battleType == 1)
				player.reduceAdena(_price, true);
			else if(_battleType == 2)
				removeExp(player, 2);

			_team2list.add(player.getStoredId());
			say(player.getName() + " equipe Usuário 2");
			if(_team1list.size() >= _team1count && _team2list.size() >= _team2count)
			{
				say("As equipes estão prontas, comece após 1 minuto.");
				_timeToStart = 1;
			}
		}
	}

	public void template_announce()
	{
		Player creator = GameObjectsStorage.getAsPlayer(_creatorId);

		if(_status != 1 || creator == null)
			return;

		if(_timeToStart > 1)
		{
			_timeToStart--;
			say(creator.getName() + " combate criado " + _team1count + "х" + _team2count + ", " + _team1min + "-" + _team1max + "lv vs " + _team2min + "-" + _team2max + "lv, taxa " + (_battleType == 1 ? _price + "а" : "experiência") + ", Recomeçar " + _timeToStart + " min");
			executeTask("events.arena." + _className, "announce", new Object[0], 60000);
		}
		else if(_team2list.size() > 0)
		{
			say("Preparar-se para a batalha");
			executeTask("events.arena." + _className, "prepare", new Object[0], 5000);
		}
		else
		{
			say("Luta não ocorreu, não houve adversários");
			_status = 0;
			if(_battleType == 1)
				returnAdenaToTeams();
			else if(_battleType == 2)
				returnExpToTeams();
			clearTeams();
		}
	}

	public void template_prepare()
	{
		if(_status != 1)
			return;

		_status = 2;
		for(Player player : getPlayers(_team1list))
			if(!player.isDead())
				_team1live.add(player.getStoredId());
		for(Player player : getPlayers(_team2list))
			if(!player.isDead())
				_team2live.add(player.getStoredId());
		if(!checkTeams())
			return;
		clearArena();
		paralyzeTeams();
		teleportTeamsToArena();
		say("Combate começará em 15 segundos");
		executeTask("events.arena." + _className, "start", new Object[0], 15000);
	}

	public void template_start()
	{
		if(_status != 2)
			return;

		if(!checkTeams())
			return;
		say("Go!!!");
		unParalyzeTeams();
		_status = 3;
		executeTask("events.arena." + _className, "timeOut", new Object[0], 180000);
		_timeOutTask = true;
	}

	public void clearArena()
	{
		for(Creature cha : _zone.getObjects())
			if(cha.isPlayable())
				cha.teleToLocation(_zone.getSpawn());
	}

	public boolean checkTeams()
	{
		if(_team1live.isEmpty())
		{
			teamHasLost(1);
			return false;
		}
		else if(_team2live.isEmpty())
		{
			teamHasLost(2);
			return false;
		}
		return true;
	}

	public void paralyzeTeams()
	{
		Skill revengeSkill = SkillTable.getInstance().getInfo(Skill.SKILL_RAID_CURSE, 1);
		for(Player player : getPlayers(_team1live))
		{
			player.getEffectList().stopEffect(Skill.SKILL_MYSTIC_IMMUNITY);
			revengeSkill.getEffects(player, player, false, false);
			if(player.getPet() != null)
				revengeSkill.getEffects(player, player.getPet(), false, false);
		}
		for(Player player : getPlayers(_team2live))
		{
			player.getEffectList().stopEffect(Skill.SKILL_MYSTIC_IMMUNITY);
			revengeSkill.getEffects(player, player, false, false);
			if(player.getPet() != null)
				revengeSkill.getEffects(player, player.getPet(), false, false);
		}
	}

	public void unParalyzeTeams()
	{
		for(Player player : getPlayers(_team1list))
		{
			player.getEffectList().stopEffect(Skill.SKILL_RAID_CURSE);
			if(player.getPet() != null)
				player.getPet().getEffectList().stopEffect(Skill.SKILL_RAID_CURSE);
		}
		for(Player player : getPlayers(_team2list))
		{
			player.getEffectList().stopEffect(Skill.SKILL_RAID_CURSE);
			if(player.getPet() != null)
				player.getPet().getEffectList().stopEffect(Skill.SKILL_RAID_CURSE);
		}
	}

	public void teleportTeamsToArena()
	{
		Integer n = 0;
		for(Player player : getPlayers(_team1live))
		{
			player.teleToLocation(_team1points.get(n));
			if(player.getPet() != null)
				player.getPet().teleToLocation(_team1points.get(n));
			player.setTeam(TeamType.BLUE);
			n++;
		}
		n = 0;
		for(Player player : getPlayers(_team2live))
		{
			player.teleToLocation(_team2points.get(n));
			if(player.getPet() != null)
				player.getPet().teleToLocation(_team2points.get(n));
			player.setTeam(TeamType.RED);
			n++;
		}
	}

	public boolean playerHasLost(Player player)
	{
		_team1live.remove(player.getStoredId());
		_team2live.remove(player.getStoredId());
		Skill revengeSkill = SkillTable.getInstance().getInfo(Skill.SKILL_RAID_CURSE, 1);
		player.getEffectList().stopEffect(Skill.SKILL_MYSTIC_IMMUNITY);
		revengeSkill.getEffects(player, player, false, false);
		return !checkTeams();
	}

	public void teamHasLost(Integer team_id)
	{
		if(team_id == 1)
		{
			say("Equipa 2 Venceu");
			if(_battleType == 1)
				payAdenaToTeam(2);
			else if(_battleType == 2)
				payExpToTeam(2);
		}
		else
		{
			say("Equipa 1 Venceu");
			if(_battleType == 1)
				payAdenaToTeam(1);
			else if(_battleType == 2)
				payExpToTeam(1);
		}
		unParalyzeTeams();
		clearTeams();
		_status = 0;
		_timeOutTask = false;
	}

	public void template_timeOut()
	{
		if(_timeOutTask && _status == 3)
		{
			say("Evento Encerrado, Combate Empatado!");
			if(_battleType == 1)
				returnAdenaToTeams();
			else if(_battleType == 2)
				returnExpToTeams();
			unParalyzeTeams();
			clearTeams();
			_status = 0;
			_timeOutTask = false;
		}
	}

	public void payAdenaToTeam(Integer team_id)
	{
		if(team_id == 1)
			for(Player player : getPlayers(_team1list))
				player.addAdena(_price + _team2list.size() * _price / _team1list.size());
		else
			for(Player player : getPlayers(_team2list))
				player.addAdena(_price + _team1list.size() * _price / _team2list.size());
	}

	public void payExpToTeam(Integer team_id)
	{
		if(team_id == 1)
			for(Player player : getPlayers(_team1list))
			{
				returnExp(player);
				addExp(player, _team2exp / _team1list.size() / 2);
			}
		else
			for(Player player : getPlayers(_team2list))
			{
				returnExp(player);
				addExp(player, _team1exp / _team2list.size() / 2);
			}
	}

	public void returnAdenaToTeams()
	{
		for(Player player : getPlayers(_team1list))
			player.addAdena(_price);
		for(Player player : getPlayers(_team2list))
			player.addAdena(_price);
	}

	public void returnExpToTeams()
	{
		for(Player player : getPlayers(_team1list))
			returnExp(player);
		for(Player player : getPlayers(_team2list))
			returnExp(player);
	}

	public void clearTeams()
	{
		for(Player player : getPlayers(_team1list))
			player.setTeam(TeamType.NONE);
		for(Player player : getPlayers(_team2list))
			player.setTeam(TeamType.NONE);
		_team1list.clear();
		_team2list.clear();
		_team1live.clear();
		_team2live.clear();
	}

	public void removeExp(Player player, int team)
	{
		int lostExp = Math.round((Experience.LEVEL[player.getLevel() + 1] - Experience.LEVEL[player.getLevel()]) * 4 / 100);
		player.addExpAndSp(-1 * lostExp, 0);
		_expToReturn.put(player.getObjectId(), lostExp);
		_classToReturn.put(player.getObjectId(), player.getActiveClassId());

		if(team == 1)
			_team1exp += lostExp;
		else if(team == 2)
			_team2exp += lostExp;
	}

	public void returnExp(Player player)
	{
		int addExp = _expToReturn.get(player.getObjectId());
		int classId = _classToReturn.get(player.getObjectId());
		if(addExp > 0 && player.getActiveClassId() == classId)
			player.addExpAndSp(addExp, 0);
	}

	public void addExp(Player player, int exp)
	{
		int classId = _classToReturn.get(player.getObjectId());
		if(player.getActiveClassId() == classId)
			player.addExpAndSp(exp, 0);
	}

	protected void onDeath(Creature self, Creature killer)
	{
		if(_status >= 2 && self.isPlayer() && (_team1list.contains(self.getStoredId()) || _team2list.contains(self.getStoredId())))
		{
			Player player = self.getPlayer();
			Player kplayer = killer.getPlayer();
			if(kplayer != null)
			{
				say(kplayer.getName() + " Assassinou " + player.getName());
				if(player.getTeam() == kplayer.getTeam() || !_team1list.contains(kplayer.getStoredId()) && !_team2list.contains(kplayer.getStoredId()))
				{
					say("um jogador Violou as regras " + kplayer.getName() + " multado " + _price);
					kplayer.reduceAdena(_price, true);
				}
				playerHasLost(player);
			}
			else
			{
				say(player.getName() + " assassinado");
				playerHasLost(player);
			}
		}
	}

	protected void onPlayerExit(Player player)
	{
		if(player != null && _status > 0 && (_team1list.contains(player.getStoredId()) || _team2list.contains(player.getStoredId())))
			switch(_status)
			{
				case 1:
					removePlayer(player);
					say(player.getName() + " desclassificado");
					if(player.getStoredId() == _creatorId)
					{
						say("Combate interrompidos taxas retornaram");
						if(_battleType == 1)
							returnAdenaToTeams();
						else if(_battleType == 2)
							returnExpToTeams();
						unParalyzeTeams();
						clearTeams();
						_status = 0;
						_timeOutTask = false;
					}
					break;
				case 2:
					removePlayer(player);
					say(player.getName() + " desclassificado");
					checkTeams();
					break;
				case 3:
					removePlayer(player);
					say(player.getName() + " desclassificado");
					checkTeams();
					break;
			}
	}

	protected void onTeleport(Player player)
	{
		if(_status > 1 && player.isInZone(_zone))
			onPlayerExit(player);
	}

	public class ZoneListener implements OnZoneEnterLeaveListener
	{
		@Override
		public void onZoneEnter(Zone zone, Creature cha)
		{
			Player player = cha.getPlayer();
			if(_status >= 2 && player != null && !(_team1list.contains(player.getStoredId()) || _team2list.contains(player.getStoredId())))
				ThreadPoolManager.getInstance().schedule(new TeleportTask(cha, _zone.getSpawn()), 3000);
		}

		@Override
		public void onZoneLeave(Zone zone, Creature cha)
		{
			Player player = cha.getPlayer();
			if(_status >= 2 && player != null && (_team1list.contains(player.getStoredId()) || _team2list.contains(player.getStoredId())))
			{
				double angle = PositionUtils.convertHeadingToDegree(cha.getHeading()); // ângulo em graus
				double radian = Math.toRadians(angle - 90); // ângulo em radianos
				int x = (int) (cha.getX() + 50 * Math.sin(radian));
				int y = (int) (cha.getY() - 50 * Math.cos(radian));
				int z = cha.getZ();
				ThreadPoolManager.getInstance().schedule(new TeleportTask(cha, new Location(x, y, z)), 3000);
			}
		}
	}

	public class TeleportTask extends RunnableImpl
	{
		Location loc;
		Creature target;

		public TeleportTask(Creature target, Location loc)
		{
			this.target = target;
			this.loc = loc;
			target.block();
		}

		@Override
		public void runImpl() throws Exception
		{
			target.unblock();
			target.teleToLocation(loc);
		}
	}

	private void removePlayer(Player player)
	{
		if(player != null)
		{
			_team1list.remove(player.getStoredId());
			_team2list.remove(player.getStoredId());
			_team1live.remove(player.getStoredId());
			_team2live.remove(player.getStoredId());
			player.setTeam(TeamType.NONE);
		}
	}

	private List<Player> getPlayers(List<Long> list)
	{
		List<Player> result = new ArrayList<Player>();
		for(Long storeId : list)
		{
			Player player = GameObjectsStorage.getAsPlayer(storeId);
			if(player != null)
				result.add(player);
		}
		return result;
	}

	public void say(String text)
	{
		Say2 cs = new Say2(0, ChatType.SHOUT, "Arena", text);
		for(Player player : GameObjectsStorage.getAllPlayersForIterate())
			if(!player.isBlockAll() && player.isInRange(_zone.getSpawn(), 4000))
				player.sendPacket(cs);
	}
}