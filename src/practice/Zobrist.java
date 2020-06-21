package practice;

import java.util.ArrayList;
import java.util.Dictionary;
import java.util.Hashtable;
import java.util.List;
import java.util.Map;
import java.util.Random;

import practice.Board.PlayerColor;

public class Zobrist 
{
	private static Zobrist m_pInstance = null;
	private Zobrist() { Init(); }
	public static Zobrist Instance() 
	{
		if(m_pInstance == null) 
		{
			m_pInstance = new Zobrist();
		}
		
		return m_pInstance;
	}
	
	public class ScoreNode 
	{
		private ScoreNode() {}
		public int score = 0;
		int dep = 0;
		public ScoreNode SetScore(int score) { this.score = score; return this; }
		public ScoreNode SetDep(int dep) { this.dep = dep; return this; }
	}
	
	private class ScoreNodePool 
	{
		private List<ScoreNode> m_arrPool = new ArrayList<>();
		public void CheckIn(ScoreNode node) 
		{
			m_arrPool.add(node);
		}
		public ScoreNode CheckOut() 
		{
			if(m_arrPool.isEmpty()) 
			{
				ScoreNode node = new ScoreNode();
				return node;
			}
			
			ScoreNode node = m_arrPool.get(m_arrPool.size() - 1);
			m_arrPool.remove(m_arrPool.size() - 1);
			return node;
		}
		
		public int GetLength() 
		{
			return m_arrPool.size();
		}
	}
	
	private Map<Long, ScoreNode> m_mapBlackHashCodeToScore, m_mapWhiteHashCodeToScore;
	private ScoreNodePool m_arrNodePool = new ScoreNodePool();
	
	private long[][][] m_stPosKey; // 记录了每个位置的hash码, --> 下棋方，棋盘位置
	private void Init() 
	{
		m_stPosKey = new long[2][Board.BOARD_SIZE][Board.BOARD_SIZE];
		Random random = new Random();
		for(int k=0;k<2;k++) 
		{
			for(int i=0;i<Board.BOARD_SIZE;i++) 
			{
				for(int j=0;j<Board.BOARD_SIZE;j++) 
				{
					m_stPosKey[k][i][j] = random.nextLong();
				}
			}
		}
		
		m_mapBlackHashCodeToScore = new Hashtable<>();
		m_mapWhiteHashCodeToScore = new Hashtable<>();
	}
	
	private int ColorToInt(PlayerColor color) 
	{
		return color == PlayerColor.Black ? 0 : 1;
	}
	
	private Map<Long, ScoreNode> GetMapByColor(PlayerColor color) 
	{
		return color == PlayerColor.Black ? m_mapBlackHashCodeToScore : m_mapWhiteHashCodeToScore;
	}
	
	public long AppendHashCode(long preHashCode, int x, int y, PlayerColor color)
	{
		return preHashCode ^ this.m_stPosKey[this.ColorToInt(color)][x][y];
	}
	
	public long UnAppendHashCode(long preHashCode, int x, int y, PlayerColor color) 
	{
		return preHashCode ^ this.m_stPosKey[this.ColorToInt(color)][x][y];
	}
	
	public ScoreNode GetScoreNodeByHashCode(long hashCode, PlayerColor nowColor) 
	{
		Map<Long, ScoreNode> map = GetMapByColor(nowColor);
		ScoreNode node = map.get(hashCode);
		return node;
	}
	
	public void SetScoreNodeByHashCode(long hashCode, PlayerColor nowColor, int score, int dep) 
	{
		Map<Long, ScoreNode> map = GetMapByColor(nowColor);
		ScoreNode node = map.get(hashCode);
		if(node == null) 
		{
			ScoreNode newNode = m_arrNodePool.CheckOut().SetScore(score).SetDep(dep);
			map.put(hashCode, newNode);
		}
		else 
		{
			node.SetScore(score).SetDep(dep);
		}
	}
	
	public void Clear() 
	{
		for (ScoreNode node : m_mapBlackHashCodeToScore.values()) 
		{
			m_arrNodePool.CheckIn(node);
		}
		for (ScoreNode node : m_mapWhiteHashCodeToScore.values()) 
		{
			m_arrNodePool.CheckIn(node);
		}
		m_mapBlackHashCodeToScore.clear();
		m_mapWhiteHashCodeToScore.clear();
		
//		System.out.println(" 局面哈希表节点数目 " + m_arrNodePool.GetLength());
	}
}
