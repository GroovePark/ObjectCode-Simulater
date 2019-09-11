package sp18_simulator;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;

/**
 * SicLoader는 프로그램을 해석해서 메모리에 올리는 역할을 수행한다. 이 과정에서 linker의 역할 또한 수행한다. 
 * <br><br>
 * SicLoader가 수행하는 일을 예를 들면 다음과 같다.<br>
 * - program code를 메모리에 적재시키기<br>
 * - 주어진 공간만큼 메모리에 빈 공간 할당하기<br>
 * - 과정에서 발생하는 symbol, 프로그램 시작주소, control section 등 실행을 위한 정보 생성 및 관리
 */
public class SicLoader {
	ResourceManager rMgr;
	
	public SicLoader(ResourceManager resourceManager) {
		// 필요하다면 초기화
		setResourceManager(resourceManager);
	}

	/**
	 * Loader와 프로그램을 적재할 메모리를 연결시킨다.
	 * @param rMgr
	 */
	public void setResourceManager(ResourceManager resourceManager) {
		this.rMgr=resourceManager;
	}
	
	/**
	 * object code를 읽어서 load과정을 수행한다. load한 데이터는 resourceManager가 관리하는 메모리에 올라가도록 한다.
	 * load과정에서 만들어진 symbol table 등 자료구조 역시 resourceManager에 전달한다.
	 * @param objectCode 읽어들인 파일
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
	
	while((line = bufferedreader.readLine()) != null) //PASS1 메모리 초기화(modify 제외),ESTAB 생성
	{	
		// 오브젝트 코드의 섹션 사이 간 공백 무시
		if(!line.equals(""))
		{	
			
			if(line.charAt(0) == 'H')
			{	
				if(sectnum == 0)
				{	
					//H레코드에서 처음 나오는 main routine의 이름 즉   program의 이름 저장
					// symboltable에 symbol 과 adress 저장
					rMgr.programname = line.substring(1, 7);
					loc = Integer.parseInt(line.substring(7, 13),6);
					rMgr.symtabList.putSymbol(line.substring(1, 7), loc);
				}
				else
				{	
					// symboltable에는 메모리상의 절대주소값이 들어가지만 objectcode 상에서는
					// 상대 주소값이 나오기 때문에 loc값에 각 프로그램의 길이를 더해줘서 주소값 구해줌
					loc += rMgr.sectorlenth.get(sectnum-1);
					rMgr.symtabList.putSymbol(line.substring(1, 7).trim(), loc);
				}
				s = line.substring(13, 19);
				
				rMgr.sectorlenth.add(Integer.parseInt(s,16));
				sectnum++;
			}
			if(line.charAt(0)=='E')
			{	
				//E레코드에서는 시작주소값 저장
				if(sa == 0)
				{
					rMgr.startaddress = line.substring(1,7);
					sa = 1;
				}	
			}
			if(line.charAt(0) == 'D')
			{	
				// D레코드에서는 정의 된 심벌들을 symboltable에 넣어준다.(trim은 앞뒤 공백 제거 메소드)
				int m = (line.length()-1) / 12;
				for (i=0;i<m;i++)
				{
					rMgr.symtabList.putSymbol(line.substring(1+12*i, 7+12*i).trim(), 
							Integer.parseInt(line.substring(7+12*i, 13+12*i),16));
				}
				
			}
			if(line.charAt(0) == 'T')
			{	
				// T레코드 일때 2바이트의 문자를 1바이트르로 패킹해서 메모리에 올린다.
				// 시작주소값 부터 길이 만큼의 바이트를 대상으로 한다.
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
	
	// pass1이 끝나고 나면 각 section의 길이를 더해서 프로그램의 전체 길이를 구한다.
	for(i=0;i<rMgr.sectorlenth.size();i++)
	{
		rMgr.program_length += rMgr.sectorlenth.get(i);
	}
	filereader.close();
	
	
	filereader = new FileReader(objectCode);
	bufferedreader = new BufferedReader(filereader);
	sectnum = -1;
	while((line = bufferedreader.readLine()) != null) //PASS2 참조된 address값 수정
	{	
		if(!line.equals(""))
		{	
			if(line.charAt(0) == 'H')
				sectnum++;
			if(line.charAt(0) == 'M')
			{	
				// M레코드에서 수정하고자 하는 adress로 가서 수정된 값을 memory에 더해주는 과정이다.
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
					//m_value에 수정하고자하는 정수값이 들어있으므로  정수값을 2바이트의 문자로 바꾸어 메모리값을 수정한다.
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
