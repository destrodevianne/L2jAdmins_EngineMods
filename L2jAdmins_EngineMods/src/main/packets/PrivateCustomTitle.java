/*
 * L2J_EngineMods
 * Engine developed by Fissban.
 *
 * This software is not free and you do not have permission
 * to distribute without the permission of its owner.
 *
 * This software is distributed only under the rule
 * of www.devsadmins.com.
 * 
 * Contact us with any questions by the media
 * provided by our web or email marco.faccio@gmail.com
 */
package main.packets;

import net.sf.l2j.gameserver.model.actor.instance.L2PcInstance;
import net.sf.l2j.gameserver.network.AbstractServerBasePacket;

/**
 * @author fissban
 */
public class PrivateCustomTitle extends AbstractServerBasePacket
{
	public static enum TitleType
	{
		SELL(0x9c),
		BUY(0xb9),
		MANUFACTURE(0xdb);
		
		private int _opCode;
		
		private TitleType(int opCode)
		{
			_opCode = opCode;
		}
		
		public int getOpCode()
		{
			return _opCode;
		}
	}
	
	private final L2PcInstance _player;
	private final int _opCode;
	private final String _msg;
	
	public PrivateCustomTitle(L2PcInstance player, TitleType titleType, String msg)
	{
		_player = player;
		_opCode = titleType.getOpCode();
		_msg = msg;
	}
	
	@Override
	public void writeImpl()
	{
		writeC(_opCode);
		writeD(_player.getObjectId());
		writeS(_msg);
	}
}
