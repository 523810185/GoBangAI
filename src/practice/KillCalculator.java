package practice;

import practice.Board.PlayerColor;

public class KillCalculator 
{
	private KillCalculator() {}
	private static KillCalculator m_pInstance = null;
	public static KillCalculator Instance() 
	{
		if(m_pInstance == null) 
		{
			m_pInstance = new KillCalculator();
		}
		
		return m_pInstance;
	}
	
	public boolean IsKillPoint(int x, int y, PlayerColor color) 
	{
		Board board = Board.Instance();
		
		if(board.IsValidPos(x, y) == false || board.IsEmptyPos(x, y) == false)
		{
			return false;
		}
		
		for(int k=0;k<Board.DIR.length;k++) 
		{
			int[] dir = Board.DIR[k];
			int dirX = dir[0];
			int dirY = dir[1];
			
			{
				int _sign = 1;
				int cnt = 1;
				boolean useEmpty = false;
				int otherX = -100, otherY = -100;
				for(int __ = 0; cnt <= 5 ; __++)
				{
					int _x = x + dirX * __ * _sign;
					int _y = y + dirY * __ * _sign;
					if(board.IsEmptyPos(_x, _y)) 
					{
						if(useEmpty) 
						{
							break;
						}
						else 
						{
							useEmpty = true;
						}
					}
					else 
					{
						if(board.IsSameColor(_x, _y, color))
						{
							cnt++;
							otherX = _x;
							otherY = _y;
						}
						else 
						{
							break;
						}
					}
				}
				boolean otherOneIsEmpty = board.IsEmptyPos(otherX + dirX * _sign, otherY + dirY * _sign);
				if(otherOneIsEmpty) 
				{
					// 活三以上是杀点
					if(cnt >= 3) 
					{
						return true;
					}
				}
				else 
				{
					// 冲四以上是杀点
					if(cnt >= 4) 
					{
						return true;
					}
				}
			}
			
			// 反个方向再判断一遍
			{
				int _sign = -1;
				int cnt = 1;
				boolean useEmpty = false;
				int otherX = -100, otherY = -100;
				for(int __ = 0; cnt <= 5 ; __++)
				{
					int _x = x + dirX * __ * _sign;
					int _y = y + dirY * __ * _sign;
					if(board.IsEmptyPos(_x, _y)) 
					{
						if(useEmpty) 
						{
							break;
						}
						else 
						{
							useEmpty = true;
						}
					}
					else 
					{
						if(board.IsSameColor(_x, _y, color))
						{
							cnt++;
							otherX = _x;
							otherY = _y;
						}
						else 
						{
							break;
						}
					}
				}
				boolean otherOneIsEmpty = board.IsEmptyPos(otherX + dirX * _sign, otherY + dirY * _sign);
				if(otherOneIsEmpty) 
				{
					// 活三以上是杀点
					if(cnt >= 3) 
					{
						return true;
					}
				}
				else 
				{
					// 冲四以上是杀点
					if(cnt >= 4) 
					{
						return true;
					}
				}
			}
		}
		
		return false;
	}
}
