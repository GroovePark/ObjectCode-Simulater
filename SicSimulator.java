package sp18_simulator;

import java.io.File;
import java.io.IOException;

import javax.swing.JOptionPane;

/**
 * 시뮬레이터로서의 작업을 담당한다. VisualSimulator에서 사용자의 요청을 받으면 이에 따라
 * ResourceManager에 접근하여 작업을 수행한다.  
 * 
 * 작성중의 유의사항 : <br>
 *  1) 새로운 클래스, 새로운 변수, 새로운 함수 선언은 얼마든지 허용됨. 단, 기존의 변수와 함수들을 삭제하거나 완전히 대체하는 것은 지양할 것.<br>
 *  2) 필요에 따라 예외처리, 인터페이스 또는 상속 사용 또한 허용됨.<br>
 *  3) 모든 void 타입의 리턴값은 유저의 필요에 따라 다른 리턴 타입으로 변경 가능.<br>
 *  4) 파일, 또는 콘솔창에 한글을 출력시키지 말 것. (채점상의 이유. 주석에 포함된 한글은 상관 없음)<br>
 * 
 * <br><br>
 *  + 제공하는 프로그램 구조의 개선방법을 제안하고 싶은 분들은 보고서의 결론 뒷부분에 첨부 바랍니다. 내용에 따라 가산점이 있을 수 있습니다.
 */
public class SicSimulator {
	ResourceManager rMgr;

	public SicSimulator(ResourceManager resourceManager) {
		// 필요하다면 초기화 과정 추가
		this.rMgr = resourceManager;
	}

	/**
	 * 레지스터, 메모리 초기화 등 프로그램 load와 관련된 작업 수행.
	 * 단, object code의 메모리 적재 및 해석은 SicLoader에서 수행하도록 한다. 
	 */
	public void load(File program) {
		/* 메모리 초기화, 레지스터 초기화 등*/
		rMgr.initializeResource();
	}

	/**
	 * 1개의 instruction이 수행된 모습을 보인다. 
	 * @throws IOException 
	  리턴 값으로는 simulator에 출력해줄 machinecode와 그 코드의 insturciton 스트링값이다.*/
	public String oneStep() throws IOException {
		String s_opcode,machinecode = "";
		int n_opcode, i;
		int TA = 0;
		
		//s_opcode는 macinecode에서 instruction을 나타내는 값의 문자열 형이다.
		n_opcode = rMgr.memory[rMgr.nowloc] &252;
		s_opcode = String.format("%02X", n_opcode);
		
		//s_opcode값을 사용해 조건문으로 각 insturction을 처리한다.
		
		//////////// STL 처리 /////////////////////////////
		//L레지스터에 있는 값을 원하는 메모리주소에 넣는다.
		if(s_opcode.equals("14")) 
		{	
				
			if(((rMgr.memory[rMgr.nowloc+1] >> 4) & 2) == 2) // PC relative
			{
				TA = rMgr.getTA(rMgr.nowloc, 3) + rMgr.nowloc + 3;
			}
			//L레지스터에 있는 값을 memory에 넣어주는과정이다.
			for(i=0;i<3;i++)
			{
				rMgr.memory[TA+i] = rMgr.intToChar(rMgr.register[2])[i];
			}
			//simulator에 출력해줄 이번에 수행된 machinecode를 구해주는 과정이다.
			//다른 insturction의 조건문에도 똑같이 쓰일 것이다.(형식에 따라 for문에서 2,3,4번 반복으로 나누어진다)
			for(i=0;i<3;i++)
			{
				int a;
				a = rMgr.memory[rMgr.nowloc+i]>>4;
				machinecode +=String.format("%01X",a);
				a = rMgr.memory[rMgr.nowloc+i]&15;
				machinecode +=String.format("%01X",a);
			}
			rMgr.nowloc += 3;
			return machinecode+"   STL";	
		}
		////////////////// JSUB 처리 //////////////////////
		// 원하는 주소로 점프한다.
		if(s_opcode.equals("48")) 
		{	
			if(((rMgr.memory[rMgr.nowloc+1] >> 4) & 1) == 1) // 4형식 체크
				TA = rMgr.getTA(rMgr.nowloc, 4);
				
			for(i=0;i<4;i++)
			{
				int a;
				a = rMgr.memory[rMgr.nowloc+i]>>4;
				machinecode +=String.format("%01X",a);
				a = rMgr.memory[rMgr.nowloc+i]&15;
				machinecode +=String.format("%01X",a);
			}
			rMgr.setRegister(2, rMgr.nowloc+4);
			rMgr.nowloc = TA; // 다음 주소값을 IMMEDIATE를 통해 구한 점프할 주소 adress로 설정한다.
			return machinecode+"   +JSUB";
		}
		///////////// CLEAR 처리 /////////////////////////////
		// 원하는 레지스터 갑을 0으로 초기화 한다.
		if(s_opcode.equals("B4"))
		{
			rMgr.setRegister(rMgr.memory[rMgr.nowloc+1]>>4, 0);
			for(i=0;i<2;i++)
			{
				int a;
				a = rMgr.memory[rMgr.nowloc+i]>>4;
				machinecode +=String.format("%01X",a);
				a = rMgr.memory[rMgr.nowloc+i]&15;
				machinecode +=String.format("%01X",a);
			}
			rMgr.nowloc += 2;
			return machinecode+"   CLEAR";	
		}
		////////LDT 처리////////////////////////////////
		// T레지스터에 원하는 위치의 메모리 값을 넣어준다
		if(s_opcode.equals("74"))
		{	
			char[] value = new char[3];
			if(((rMgr.memory[rMgr.nowloc+1] >> 4) & 1) == 1) //4형식일때
			{
				TA = rMgr.getTA(rMgr.nowloc, 4);
				value = rMgr.getMemory(TA, 3);
				rMgr.setRegister(5, rMgr.byteToInt(value));
				for(i=0;i<4;i++)
				{
					int a;
					a = rMgr.memory[rMgr.nowloc+i]>>4;
					machinecode +=String.format("%01X",a);
					a = rMgr.memory[rMgr.nowloc+i]&15;
					machinecode +=String.format("%01X",a);
				}
				rMgr.nowloc += 4;
				return machinecode+"   +LDT";
			}
			else //3형식
			{
				if(((rMgr.memory[rMgr.nowloc+1] >> 4) & 2) == 2) // PC relative
				{	
					TA = rMgr.getTA(rMgr.nowloc, 3) + rMgr.nowloc + 3;
				}
				value = rMgr.getMemory(TA, 3);
				rMgr.setRegister(5, rMgr.byteToInt(value));
				for(i=0;i<3;i++)
				{
					int a;
					a = rMgr.memory[rMgr.nowloc+i]>>4;
					machinecode +=String.format("%01X",a);
					a = rMgr.memory[rMgr.nowloc+i]&15;
					machinecode +=String.format("%01X",a);
				}
				rMgr.nowloc += 3;
				return machinecode+"   LDT";
			}
		}
		///////// TD 처리 //////////////////////////////////
		// DEVICE가 준비됬는 지 확인하는 과정이다. 준비되면 PC레지스터에 1 세팅, 안되면 0을 세팅한다.
		if(s_opcode.equals("E0"))
		{	
			String devName = "";
			String s = "";
			char c;
			c = rMgr.memory[rMgr.getTA(rMgr.nowloc, 3) + rMgr.nowloc +3];
			s = String.format("%01X", c >> 4);
			devName +=s;
			s = String.format("%01X", c & 15);
			devName +=s;
			rMgr.testDevice(devName+".txt");
			for(i=0;i<3;i++)
			{
				int a;
				a = rMgr.memory[rMgr.nowloc+i]>>4;
				machinecode +=String.format("%01X",a);
				a = rMgr.memory[rMgr.nowloc+i]&15;
				machinecode +=String.format("%01X",a);
			}
			rMgr.nowloc += 3;
			return machinecode+"   TD";
		}
		////////// JEQ 처리  ////////////////////////////////////
		//pc레지스터 값에 따라 점프할지 그냥 넘어갈지 결정한다.
		if(s_opcode.equals("30"))
		{	
			for(i=0;i<3;i++)
			{
				int a;
				a = rMgr.memory[rMgr.nowloc+i]>>4;
				machinecode +=String.format("%01X",a);
				a = rMgr.memory[rMgr.nowloc+i]&15;
				machinecode +=String.format("%01X",a);
			}
			
			if(rMgr.register[9] == 0) // 같으므로 operand 주소로 점프
			{	
				/// tooth compliment 사용
				if(rMgr.getTA(rMgr.nowloc, 3)> 4000)
					rMgr.nowloc = rMgr.getTA(rMgr.nowloc, 3) +rMgr.nowloc + 3 - 4096;
				else
					rMgr.nowloc = rMgr.getTA(rMgr.nowloc, 3)+ rMgr.nowloc + 3;
			}
			else // 다르므로 다음 instruction으로 넘어간다
			{
				rMgr.nowloc += 3;
			}
			return machinecode+"   JEQ";
		}
		/////////// RD 처리 ////////////////////////////////
		// Device에서 문자하나를 읽어서 A레지스터에 저장한다.
		if(s_opcode.equals("D8"))
		{	
			String devName = "";
			String s = "";
			char c;
			c = rMgr.memory[rMgr.getTA(rMgr.nowloc, 3) + rMgr.nowloc +3];
			s = String.format("%01X", c >> 4);
			devName +=s;
			s = String.format("%01X", c & 15);
			devName +=s;
			devName += ".txt";
			rMgr.register[0] = rMgr.readDevice(devName); // A레지스터에 읽은 데이타 저장
			if(rMgr.register[0] == 48)  // 48이 저장되었다는 것은 0이란 문자를 읽었다는 것이므로 정수 0값을 대신 넣어준다. 
				rMgr.register[0] = 0;
			
			for(i=0;i<3;i++)
			{
				int a;
				a = rMgr.memory[rMgr.nowloc+i]>>4;
				machinecode +=String.format("%01X",a);
				a = rMgr.memory[rMgr.nowloc+i]&15;
				machinecode +=String.format("%01X",a);
			}
			rMgr.nowloc += 3;
			return machinecode+"   RD";
		}
		/////////// COMPR 처리 ///////////////////////////////////////
		// 레지스터 2개의 갑을 서로 비교해서 pc레지스터 값을 세팅한다.
		if(s_opcode.equals("A0"))
		{
			if(rMgr.getRegister(rMgr.memory[rMgr.nowloc+1]>>4) == 
					rMgr.getRegister(rMgr.memory[rMgr.nowloc+1]&15))
			{
				rMgr.setRegister(9, 0); // 같으면 flag 0으로 세팅
			}
			else if(rMgr.getRegister(rMgr.memory[rMgr.nowloc+1]>>4) < 
					rMgr.getRegister(rMgr.memory[rMgr.nowloc+1]&15))
			{
				rMgr.setRegister(9, -1); // 앞의 값이 작으면 flag -1으로 세팅
			}
			else if(rMgr.getRegister(rMgr.memory[rMgr.nowloc+1]>>4) == 
					rMgr.getRegister(rMgr.memory[rMgr.nowloc+1]&15))
			{
				rMgr.setRegister(9, 1); // 앞의 값이 크면 flag 1으로 세팅
			}
			
			for(i=0;i<2;i++)
			{
				int a;
				a = rMgr.memory[rMgr.nowloc+i]>>4;
				machinecode +=String.format("%01X",a);
				a = rMgr.memory[rMgr.nowloc+i]&15;
				machinecode +=String.format("%01X",a);
			}
			rMgr.nowloc += 2;
			return machinecode+"   COMPR";
		}
		//////// STCH 처리  //////////////////////////////////////
		/// A레지스터에 있는 값을 메모리에 넣어준다
		if(s_opcode.equals("54"))
		{	
			int format4 = 0;
			if(((rMgr.memory[rMgr.nowloc+1] >> 4) & 1) == 1) // 4형식 체크
			{	
				format4 = 1;
				TA = rMgr.getTA(rMgr.nowloc, 4);
				for(i=0;i<4;i++)
				{
					int a;
					a = rMgr.memory[rMgr.nowloc+i]>>4;
					machinecode +=String.format("%01X",a);
					a = rMgr.memory[rMgr.nowloc+i]&15;
					machinecode +=String.format("%01X",a);
				}
			}
			if(((rMgr.memory[rMgr.nowloc+1] >> 4) & 8) == 8)// index 체크
				//원하는 주소의 index를 더한 값에 A레지스터 값 넣어준다
				rMgr.memory[TA+rMgr.getRegister(1)] = (char) rMgr.getRegister(0); 
			
			if(format4 == 1)
			{	
				rMgr.nowloc += 4;
				return machinecode+"   +STCH";
			}
			else if(format4 == 0)
			{
				rMgr.nowloc += 3;
				return machinecode+"   STCH";
			}
		}
		
		////////TIXR 처리  //////////////////////////////////////
		/// X레지스터 값을 증가시킨뒤 원하는 레지스터 값과 비교해서 PC레지스터 값을 세팅한다.
		if(s_opcode.equals("B8"))
		{	
			rMgr.register[1]++; // X레지스터 값 1 증가시킨 뒤 비교
			if( rMgr.getRegister(1)== rMgr.getRegister(rMgr.memory[rMgr.nowloc+1]>>4))
			{
				rMgr.setRegister(9, 0); // 같으면 flag 0으로 세팅
			}
			else if( rMgr.getRegister(1)< rMgr.getRegister(rMgr.memory[rMgr.nowloc+1]>>4))
			{
				rMgr.setRegister(9, -1); // X가 작으면 flag -1으로 세팅
			}
			else if( rMgr.getRegister(1)> rMgr.getRegister(rMgr.memory[rMgr.nowloc+1]>>4))
			{
				rMgr.setRegister(9, 1); // X가 크면 flag 1으로 세팅
			}
			for(i=0;i<2;i++)
			{
				int a;
				a = rMgr.memory[rMgr.nowloc+i]>>4;
				machinecode +=String.format("%01X",a);
				a = rMgr.memory[rMgr.nowloc+i]&15;
				machinecode +=String.format("%01X",a);
			}
			
			rMgr.nowloc += 2;
			return machinecode+"   TIXR";
		}
		//////////// JLT 처리   ///////////////////////////////////
		// PC레지스터값이 -1일시 원하는 곳으로 점프
		if(s_opcode.equals("38"))
		{
			for(i=0;i<3;i++)
			{
				int a;
				a = rMgr.memory[rMgr.nowloc+i]>>4;
				machinecode +=String.format("%01X",a);
				a = rMgr.memory[rMgr.nowloc+i]&15;
				machinecode +=String.format("%01X",a);
			}
			
			if(rMgr.register[9] < 0) // FLAG 값이 작을시에 TA로 점프한다.
			{	
				/// tooth compliment 사용
				if(rMgr.getTA(rMgr.nowloc, 3)> 4000)
					rMgr.nowloc = rMgr.getTA(rMgr.nowloc, 3) +rMgr.nowloc + 3 - 4096;
				else
					rMgr.nowloc = rMgr.getTA(rMgr.nowloc, 3)+ rMgr.nowloc + 3;
			}
			else // 작지 않으 다음 instruction으로 넘어간다
			{
				rMgr.nowloc += 3;
			}
			return machinecode+"   JLT";
		}
		/////// STX 처리  ///////////////////////////////////
		/// X 레지스터에 있는 값을 원하는 메모리 주소에 넣어준다.
		if(s_opcode.equals("10"))
		{	
			int format4 = 0;
			if(((rMgr.memory[rMgr.nowloc+1] >> 4) & 1) == 1) // 4형식 체크
			{
				TA = rMgr.getTA(rMgr.nowloc, 4);
				format4 = 1;
			}	
			for(i=0;i<4;i++)
			{
				int a;
				a = rMgr.memory[rMgr.nowloc+i]>>4;
				machinecode +=String.format("%01X",a);
				a = rMgr.memory[rMgr.nowloc+i]&15;
				machinecode +=String.format("%01X",a);
			}
			for(i=0;i<3;i++)
			{	
				// 원하는 메모리 주소에 레지스터 값을 넣어준다.
				rMgr.memory[TA+i] = rMgr.intToChar(rMgr.register[1])[i];
			}
			if(format4 == 0)
			{	
				rMgr.nowloc += 3;		
				return machinecode+"   STX";
			}
			if(format4 == 1)
			{	
				rMgr.nowloc += 4;
				return machinecode+"   +STX";
			}
		}
		//////// RSUB 처리 //////////////////////////
		// L레지스터에 저장된 주소값으로 돌아간다.
		if(s_opcode.equals("4C"))
		{
			for(i=0;i<3;i++)
			{
				int a;
				a = rMgr.memory[rMgr.nowloc+i]>>4;
				machinecode +=String.format("%01X",a);
				a = rMgr.memory[rMgr.nowloc+i]&15;
				machinecode +=String.format("%01X",a);
			}
			rMgr.nowloc = rMgr.getRegister(2); // L레지스터에 저장된 값으로 리턴한다
			return machinecode+"   RSUB";
		}
		//////// LDA 처리 ///////////////////////////
		// 원하는 주소 메모리의 값을 A레지스터에 넣어준다.
		if(s_opcode.equals("00"))
		{	
			for(i=0;i<3;i++)
			{
				int a;
				a = rMgr.memory[rMgr.nowloc+i]>>4;
				machinecode +=String.format("%01X",a);
				a = rMgr.memory[rMgr.nowloc+i]&15;
				machinecode +=String.format("%01X",a);
			}
			if(((rMgr.memory[rMgr.nowloc+1] >> 4) & 2) == 2) // PC relative
			{	
				char[] value = new char[3];
				TA = rMgr.getTA(rMgr.nowloc, 3) + rMgr.nowloc + 3;
				for(i=0;i<3;i++)
				{
					value[i] = rMgr.memory[TA+i];
					
				}
				//원하는 주소의 메모리값을 A레지스터에 저장
				rMgr.setRegister(0, rMgr.byteToInt(value));
			}
			else if((rMgr.memory[rMgr.nowloc] & 3) == 1) // IMMEDIATE
			{
				TA = rMgr.getTA(rMgr.nowloc, 3);
				rMgr.setRegister(0, TA); // IMMEDIATE 값 A레지스터에 저장
			}
			
			rMgr.nowloc += 3;
			return machinecode+"   LDA";
			
		}
		
		///////// COMP 처리  ////////////////////////////////
		///// A레지스터와 원하는 값을 비교해서 PC레지스터에 값을 세팅
		if(s_opcode.equals("28"))
		{
			if((rMgr.memory[rMgr.nowloc] & 3) == 1) // IMMEDIATE
			{
				TA = rMgr.getTA(rMgr.nowloc, 3);
				if( rMgr.getRegister(0)== TA)
				{
					rMgr.setRegister(9, 0); // 같으면 flag 0으로 세팅
				}
				else if( rMgr.getRegister(0)< TA)
				{
					rMgr.setRegister(9, -1); // X가 작으면 flag -1으로 세팅
				}
				else if( rMgr.getRegister(0)> TA)
				{
					rMgr.setRegister(9, 1); // X가 크면 flag 1으로 세팅
				}
			}
			for(i=0;i<3;i++)
			{
				int a;
				a = rMgr.memory[rMgr.nowloc+i]>>4;
				machinecode +=String.format("%01X",a);
				a = rMgr.memory[rMgr.nowloc+i]&15;
				machinecode +=String.format("%01X",a);
			}
			rMgr.nowloc += 3;
			return machinecode+"   COMP";
		}
		////// LDCH 처리 /////////////////////////
		///원하는 주소의 메모리에서 한바이트를 읽어서 A레지스터에 저장한다. LDA와 다른 점은 한바이트만 읽는 다는 것이다.
		if(s_opcode.equals("50"))
		{	
			if(((rMgr.memory[rMgr.nowloc+1] >> 4) & 1) == 1) //4형식일때
			{
				TA = rMgr.getTA(rMgr.nowloc, 4);
				rMgr.register[0] = rMgr.memory[TA+rMgr.getRegister(1)]; // 값 저장하는 과정\
				for(i=0;i<4;i++)
				{
					int a;
					a = rMgr.memory[rMgr.nowloc+i]>>4;
					machinecode +=String.format("%01X",a);
					a = rMgr.memory[rMgr.nowloc+i]&15;
					machinecode +=String.format("%01X",a);
				}
				rMgr.nowloc += 4;
				return machinecode+"   +LDCH";
			}
		}
		
		////// WD 처리  /////////////////////////////
		// 디바이스가 준비 되었으면 A레지스터에 저장된 값을 write한다.
		if(s_opcode.equals("DC"))
		{
			String devName = "";
			String s = "";
			char c;
			
			//메모리에 저장되있는 device의 이름을 구하는 과정
			c = rMgr.memory[rMgr.getTA(rMgr.nowloc, 3) + rMgr.nowloc +3];
			s = String.format("%01X", c >> 4);
			devName +=s;
			s = String.format("%01X", c & 15);
			devName +=s;
			devName += ".txt";
			rMgr.writeDevice(devName,(char) rMgr.getRegister(0)); // A레지스터에 들어있는 값을 device에 쓴다
			for(i=0;i<3;i++)
			{
				int a;
				a = rMgr.memory[rMgr.nowloc+i]>>4;
				machinecode +=String.format("%01X",a);
				a = rMgr.memory[rMgr.nowloc+i]&15;
				machinecode +=String.format("%01X",a);
			}
			rMgr.nowloc += 3;
			return machinecode+"   WD";
		}
		
		////// J 처리  /////////////////////////////
		/// 원하는 위치의 메모리로 점프한다.
		if(s_opcode.equals("3C"))
		{	
			for(i=0;i<3;i++)
			{
				int a;
				a = rMgr.memory[rMgr.nowloc+i]>>4;
				machinecode +=String.format("%01X",a);
				a = rMgr.memory[rMgr.nowloc+i]&15;
				machinecode +=String.format("%01X",a);
			}
			
			if((rMgr.memory[rMgr.nowloc] & 3) == 2) // INDIRECT 처리
			{	
				char[] value = new char[3];
				TA = rMgr.getTA(rMgr.nowloc, 3) + rMgr.nowloc + 3;
				value = rMgr.getMemory(TA, 3);
				rMgr.nowloc = rMgr.byteToInt(value);

			}
			else // DIRECT 처리
			{
				/// tooth compliment 사용
				if(rMgr.getTA(rMgr.nowloc, 3)> 4000)
					rMgr.nowloc = rMgr.getTA(rMgr.nowloc, 3) +rMgr.nowloc + 3 - 4096;
				else
					rMgr.nowloc = rMgr.getTA(rMgr.nowloc, 3)+ rMgr.nowloc + 3;
			}
			return machinecode+"   J";
		}
		
		//////// STA 처리 ///////////////////////////
		/// A레지스터에 들어있는 값을 원하는 위치의 메모리에 저장한다.
		if(s_opcode.equals("0C"))
		{
			TA = rMgr.getTA(rMgr.nowloc, 3) + rMgr.nowloc + 3;
			for(i=0;i<3;i++)
			{
				rMgr.memory[TA+i] = rMgr.intToChar(rMgr.register[0])[i]; // 메모리에 A레지스터 값 저장한다.
			}
			for(i=0;i<3;i++)
			{
				int a;
				a = rMgr.memory[rMgr.nowloc+i]>>4;
				machinecode +=String.format("%01X",a);
				a = rMgr.memory[rMgr.nowloc+i]&15;
				machinecode +=String.format("%01X",a);
			}
			rMgr.nowloc += 3;
			return machinecode+"   STA";
		}
		
		return null;
	}
	
}
