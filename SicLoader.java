package sp18_simulator;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;

/**
 * SicLoader�� ���α׷��� �ؼ��ؼ� �޸𸮿� �ø��� ������ �����Ѵ�. �� �������� linker�� ���� ���� �����Ѵ�. 
 * <br><br>
 * SicLoader�� �����ϴ� ���� ���� ��� ������ ����.<br>
 * - program code�� �޸𸮿� �����Ű��<br>
 * - �־��� ������ŭ �޸𸮿� �� ���� �Ҵ��ϱ�<br>
 * - �������� �߻��ϴ� symbol, ���α׷� �����ּ�, control section �� ������ ���� ���� ���� �� ����
 */
public class SicLoader {
	ResourceManager rMgr;
	
	public SicLoader(ResourceManager resourceManager) {
		// �ʿ��ϴٸ� �ʱ�ȭ
		setResourceManager(resourceManager);
	}

	/**
	 * Loader�� ���α׷��� ������ �޸𸮸� �����Ų��.
	 * @param rMgr
	 */
	public void setResourceManager(ResourceManager resourceManager) {
		this.rMgr=resourceManager;
	}
	
	/**
	 * object code�� �о load������ �����Ѵ�. load�� �����ʹ� resourceManager�� �����ϴ� �޸𸮿� �ö󰡵��� �Ѵ�.
	 * load�������� ������� symbol table �� �ڷᱸ�� ���� resourceManager�� �����Ѵ�.
	 * @param objectCode �о���� ����
	 * @throws IOException 
	 */
	public void load(File objectCode) throws IOException{
	FileReader filereader = new FileReader(objectCode);
	BufferedReader bufferedreader = new BufferedReader(filereader);
	int i,j,a,b,c,d,sectnum,sa;
	int loc = 0;;
	sectnum = sa = 0;
	String line,s = null;
	rMgr.sectorlenth = new ArrayList<Integer>();
	rMgr.symtabList.symbolList = new ArrayList<String>();
	rMgr.symtabList.addressList = new ArrayList<Integer>();
	char[] data = new char[6];
	
	while((line = bufferedreader.readLine()) != null) //PASS1 �޸� �ʱ�ȭ(modify ����),ESTAB ����
	{	
		// ������Ʈ �ڵ��� ���� ���� �� ���� ����
		if(!line.equals(""))
		{	
			
			if(line.charAt(0) == 'H')
			{	
				if(sectnum == 0)
				{	
					//H���ڵ忡�� ó�� ������ main routine�� �̸� ��   program�� �̸� ����
					// symboltable�� symbol �� adress ����
					rMgr.programname = line.substring(1, 7);
					loc = Integer.parseInt(line.substring(7, 13),6);
					rMgr.symtabList.putSymbol(line.substring(1, 7), loc);
				}
				else
				{	
					// symboltable���� �޸𸮻��� �����ּҰ��� ������ objectcode �󿡼���
					// ��� �ּҰ��� ������ ������ loc���� �� ���α׷��� ���̸� �����༭ �ּҰ� ������
					loc += rMgr.sectorlenth.get(sectnum-1);
					rMgr.symtabList.putSymbol(line.substring(1, 7).trim(), loc);
				}
				s = line.substring(13, 19);
				
				rMgr.sectorlenth.add(Integer.parseInt(s,16));
				sectnum++;
			}
			if(line.charAt(0)=='E')
			{	
				//E���ڵ忡���� �����ּҰ� ����
				if(sa == 0)
				{
					rMgr.startaddress = line.substring(1,7);
					sa = 1;
				}	
			}
			if(line.charAt(0) == 'D')
			{	
				// D���ڵ忡���� ���� �� �ɹ����� symboltable�� �־��ش�.(trim�� �յ� ���� ���� �޼ҵ�)
				int m = (line.length()-1) / 12;
				for (i=0;i<m;i++)
				{
					rMgr.symtabList.putSymbol(line.substring(1+12*i, 7+12*i).trim(), 
							Integer.parseInt(line.substring(7+12*i, 13+12*i),16));
				}
				
			}
			if(line.charAt(0) == 'T')
			{	
				// T���ڵ� �϶� 2����Ʈ�� ���ڸ� 1����Ʈ���� ��ŷ�ؼ� �޸𸮿� �ø���.
				// �����ּҰ� ���� ���� ��ŭ�� ����Ʈ�� ������� �Ѵ�.
				int start = Integer.parseInt(line.substring(1, 7),16);
				int length = Integer.parseInt(line.substring(7, 9),16);
				char pack;
				for(i=start,j=0 ;i<length+start;i++,j++)
				{
					pack = 0;
					pack = (char) (pack | Integer.parseInt(line.substring(2*j+9,2*j+11),16));
					rMgr.memory[i+loc] =  pack;
				}
			}
		}
	}
	
	// pass1�� ������ ���� �� section�� ���̸� ���ؼ� ���α׷��� ��ü ���̸� ���Ѵ�.
	for(i=0;i<rMgr.sectorlenth.size();i++)
	{
		rMgr.program_length += rMgr.sectorlenth.get(i);
	}
	filereader.close();
	
	
	filereader = new FileReader(objectCode);
	bufferedreader = new BufferedReader(filereader);
	sectnum = -1;
	while((line = bufferedreader.readLine()) != null) //PASS2 ������ address�� ����
	{	
		if(!line.equals(""))
		{	
			if(line.charAt(0) == 'H')
				sectnum++;
			if(line.charAt(0) == 'M')
			{	
				// M���ڵ忡�� �����ϰ��� �ϴ� adress�� ���� ������ ���� memory�� �����ִ� �����̴�.
				if(line.substring(7,9).equals("05") || line.substring(7,9).equals("06") )
				{	
					int adress = Integer.parseInt(line.substring(1,7),16);
					int m_value = 0,index = 0;
					for(i=0;i<sectnum;i++)
						adress += rMgr.sectorlenth.get(i);
					a = rMgr.memory[adress+1]>>4;
					b = rMgr.memory[adress+1]&15;
					c = rMgr.memory[adress+2]>>4;
					d = rMgr.memory[adress+2]&15;
					s = String.format("%01X",a)+String.format("%01X",b)
					+String.format("%01X",c)+String.format("%01X",d);
					if(line.charAt(9) == '+')
					{
						
						index = rMgr.symtabList.symbolList.indexOf(line.substring(10,line.length()));
						m_value = Integer.parseInt(s,16) + rMgr.symtabList.addressList.get(index);
						
					}
					if(line.charAt(9) == '-')
					{
						
						index = rMgr.symtabList.symbolList.indexOf(line.substring(10,line.length()));
						
						m_value = Integer.parseInt(s,16) - rMgr.symtabList.addressList.get(index);
						System.out.println(String.format("%04X", m_value));
						
					}
					//m_value�� �����ϰ����ϴ� �������� ��������Ƿ�  �������� 2����Ʈ�� ���ڷ� �ٲپ� �޸𸮰��� �����Ѵ�.
					char pack = 0;
					pack = (char) (pack | Integer.parseInt(String.format("%04X", m_value).substring(0,2),16));
					rMgr.memory[adress+1] =  pack;
					pack = 0;
					pack = (char) (pack | Integer.parseInt(String.format("%04X", m_value).substring(2,4),16));
					rMgr.memory[adress+2] =  pack;
				}
			}
		}
	}
	};
}
