package practice;

import practice.Board.PlayerColor;

public class ScoreEvaluator 
{
	private ScoreEvaluator() { this.Init(); }
	private static ScoreEvaluator m_pInstance = null;
	public static ScoreEvaluator Instance()
	{
		if(m_pInstance == null) 
		{
			m_pInstance = new ScoreEvaluator();
		}
		
		return m_pInstance;
	}
	
	private Board m_pHandler = null;
	private int m_iBoardSize = 1;
	private void Init() 
	{
		m_iBoardSize = Board.BOARD_SIZE;
		m_pHandler = Board.Instance();
	}
	
	private PlayerColor m_stNowColor = null;
	public ScoreEvaluator SetNowPlayer(PlayerColor nowColor)
	{
		this.m_stNowColor = nowColor;
		return this;
	}
	public static final float MAX = 100000000;
	public static final float MIN = -MAX;
	private static final float GO_FIVE_SCORE = 10000000; 
	private static final float LIVE_FOUR_SCORE = 100000;
	private static final float CHONG_FOUR_SCORE = 10000;
	private static final float LIVE_THREE_SCORE = 10000;
	private static final float SLEEP_THREE_SCORE = 500;
	private static final float LIVE_TWO_SCORE = 20;
	private static final float SLEEP_TWO_SCORE = 10; 
	private static final float PLAYER_SCORE_RATE = -0.5f;
	public float GetBoardScore(boolean isAIPlay)
	{
		ResetVisRecorder();
		SetNowPlayer(Board.Instance().GetAIColor());
		Board ctx = Board.Instance();
		
		float score = 0;
		
		for(int k=0;k<Board.DIR.length;k++) 
		{
			int[] dir = Board.DIR[k];
			int _dirX = dir[0];
			int _dirY = dir[1];
			
			for(int i=0;i<m_iBoardSize;i++) 
			{
				for(int j=0;j<m_iBoardSize;j++) 
				{
					if(m_arrVisRecorder[i][j] == k) 
					{
						continue;
					}
					
					if(ctx.IsEmptyPos(i, j)) 
					{
						continue;
					}
					
					m_arrVisRecorder[i][j] = k;
					int cnt = 1; // ��Ŀ
					boolean useEmptyChance = false;
					int _RX = -1; // ���ұߵ�һ��ͬɫ
					int _RY = -1;
					for(int step=1;step<9;step++) 
					{
						int _testX = i + _dirX * step;
						int _testY = j + _dirY * step;
						if(ctx.IsValidPos(_testX, _testY) == false) 
						{
							break;
						}
						if(ctx.IsEmptyPos(_testX, _testY)) 
						{
							if(!useEmptyChance) 
							{
								if(ctx.IsSameColor(i, j, _testX + _dirX, _testY + _dirY))
								{
									useEmptyChance = true;
									continue;
								}
								else
								{
									break;
								}
							}
							else 
							{
								break;
							}
						}
						
						if(ctx.IsSameColor(i, j, _testX, _testY)) 
						{
							cnt++;
							m_arrVisRecorder[_testX][_testY] = k;
							_RX = _testX;
							_RY = _testY;
						}
						else 
						{
							break;
						}
					}
					
					// ���
					boolean isAIColor = ctx.IsAIColorAtPos(i, j);
					int _LoneX = i - _dirX;
					int _LoneY = j - _dirY;
					int _RoneX = _RX + _dirX;
					int _RoneY = _RY + _dirY;
					boolean _LoneIsEmpty = ctx.IsEmptyPos(_LoneX, _LoneY);
					boolean _RoneIsEmpty = ctx.IsEmptyPos(_RoneX, _RoneY);
					float rate = GetScoreRateByIsAIColorAtPos(isAIColor, isAIPlay);
					if(cnt >= 5) 
					{
						if(useEmptyChance) 
						{
							score += CHONG_FOUR_SCORE * rate;
						}
						else 
						{
							score += GO_FIVE_SCORE * rate;
						}
					}
					else if(cnt == 4) 
					{
						if(useEmptyChance) 
						{
							score += CHONG_FOUR_SCORE * rate;
						}
						else 
						{
							if(_LoneIsEmpty && _RoneIsEmpty) 
							{
								score += LIVE_FOUR_SCORE * rate;
							}
							else if(!_LoneIsEmpty && !_RoneIsEmpty) 
							{
								// ������ס
							}
							else 
							{
								// ����סһ��
								score += CHONG_FOUR_SCORE * rate;
								// TODO..����������Լ������Ժ�Եз����ĵľ���ȣ���Ȼ���Զ�һ�������������λ�ã������޸������������Ը����֦��
								if(isAIPlay && isAIColor == false) 
								{
									score -= LIVE_FOUR_SCORE;
								}
								else if(!isAIPlay && isAIColor) 
								{
									score += LIVE_FOUR_SCORE;
								}
							}
						}
					}
					else if(cnt == 3)
					{
						if(_LoneIsEmpty && _RoneIsEmpty) 
						{
							score += LIVE_THREE_SCORE * rate;
						}
						else if(!_LoneIsEmpty && !_RoneIsEmpty) 
						{
							// ������ס
						}
						else 
						{
							// ����סһ��
							score += SLEEP_THREE_SCORE * rate;
						}
					}
					else if(cnt == 2) 
					{
						if(_LoneIsEmpty && _RoneIsEmpty) 
						{
							score += LIVE_TWO_SCORE * rate;
						}
						else if(!_LoneIsEmpty && !_RoneIsEmpty) 
						{
							// ������ס
						}
						else 
						{
							// ����סһ��
							score += SLEEP_TWO_SCORE * rate;
						}
					}
				}
			}
		}
		
		return score;
	}
	
	private float GetScoreRateByIsAIColorAtPos(boolean isAIColor, boolean isAIPlay) 
	{
		if(isAIPlay) 
		{
			if(isAIColor) 
			{
				return 1f;
			}
			else 
			{
				return -2f;
			}
		}
		else 
		{
			if(isAIColor) 
			{
				return 2f;
			}
			else 
			{
				return -1f;
			}
		}
	}
	
	private int[][] m_arrVisRecorder = new int[Board.BOARD_SIZE][Board.BOARD_SIZE];
	private void ResetVisRecorder()
	{
		for(int i=0;i<m_arrVisRecorder.length;i++) 
		{
			int[] oneDArr = m_arrVisRecorder[i];
			for(int j=0;j<oneDArr.length;j++) 
			{
				oneDArr[j] = -1;
			}
		}
	}
}
