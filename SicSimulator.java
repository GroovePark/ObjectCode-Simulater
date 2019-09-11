package sp18_simulator;

import java.io.File;
import java.io.IOException;

import javax.swing.JOptionPane;

/**
 * �ùķ����ͷμ��� �۾��� ����Ѵ�. VisualSimulator���� ������� ��û�� ������ �̿� ����
 * ResourceManager�� �����Ͽ� �۾��� �����Ѵ�.  
 * 
 * �ۼ����� ���ǻ��� : <br>
 *  1) ���ο� Ŭ����, ���ο� ����, ���ο� �Լ� ������ �󸶵��� ����. ��, ������ ������ �Լ����� �����ϰų� ������ ��ü�ϴ� ���� ������ ��.<br>
 *  2) �ʿ信 ���� ����ó��, �������̽� �Ǵ� ��� ��� ���� ����.<br>
 *  3) ��� void Ÿ���� ���ϰ��� ������ �ʿ信 ���� �ٸ� ���� Ÿ������ ���� ����.<br>
 *  4) ����, �Ǵ� �ܼ�â�� �ѱ��� ��½�Ű�� �� ��. (ä������ ����. �ּ��� ���Ե� �ѱ��� ��� ����)<br>
 * 
 * <br><br>
 *  + �����ϴ� ���α׷� ������ ��������� �����ϰ� ���� �е��� ������ ��� �޺κп� ÷�� �ٶ��ϴ�. ���뿡 ���� �������� ���� �� �ֽ��ϴ�.
 */
public class SicSimulator {
	ResourceManager rMgr;

	public SicSimulator(ResourceManager resourceManager) {
		// �ʿ��ϴٸ� �ʱ�ȭ ���� �߰�
		this.rMgr = resourceManager;
	}

	/**
	 * ��������, �޸� �ʱ�ȭ �� ���α׷� load�� ���õ� �۾� ����.
	 * ��, object code�� �޸� ���� �� �ؼ��� SicLoader���� �����ϵ��� �Ѵ�. 
	 */
	public void load(File program) {
		/* �޸� �ʱ�ȭ, �������� �ʱ�ȭ ��*/
		rMgr.initializeResource();
	}

	/**
	 * 1���� instruction�� ����� ����� ���δ�. 
	 * @throws IOException 
	  ���� �����δ� simulator�� ������� machinecode�� �� �ڵ��� insturciton ��Ʈ�����̴�.*/
	public String oneStep() throws IOException {
		String s_opcode,machinecode = "";
		int n_opcode, i;
		int TA = 0;
		
		//s_opcode�� macinecode���� instruction�� ��Ÿ���� ���� ���ڿ� ���̴�.
		n_opcode = rMgr.memory[rMgr.nowloc] &252;
		s_opcode = String.format("%02X", n_opcode);
		
		//s_opcode���� ����� ���ǹ����� �� insturction�� ó���Ѵ�.
		
		//////////// STL ó�� /////////////////////////////
		//L�������Ϳ� �ִ� ���� ���ϴ� �޸��ּҿ� �ִ´�.
		if(s_opcode.equals("14")) 
		{	
				
			if(((rMgr.memory[rMgr.nowloc+1] >> 4) & 2) == 2) // PC relative
			{
				TA = rMgr.getTA(rMgr.nowloc, 3) + rMgr.nowloc + 3;
			}
			//L�������Ϳ� �ִ� ���� memory�� �־��ִ°����̴�.
			for(i=0;i<3;i++)
			{
				rMgr.memory[TA+i] = rMgr.intToChar(rMgr.register[2])[i];
			}
			//simulator�� ������� �̹��� ����� machinecode�� �����ִ� �����̴�.
			//�ٸ� insturction�� ���ǹ����� �Ȱ��� ���� ���̴�.(���Ŀ� ���� for������ 2,3,4�� �ݺ����� ����������)
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
		////////////////// JSUB ó�� //////////////////////
		// ���ϴ� �ּҷ� �����Ѵ�.
		if(s_opcode.equals("48")) 
		{	
			if(((rMgr.memory[rMgr.nowloc+1] >> 4) & 1) == 1) // 4���� üũ
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
			rMgr.nowloc = TA; // ���� �ּҰ��� IMMEDIATE�� ���� ���� ������ �ּ� adress�� �����Ѵ�.
			return machinecode+"   +JSUB";
		}
		///////////// CLEAR ó�� /////////////////////////////
		// ���ϴ� �������� ���� 0���� �ʱ�ȭ �Ѵ�.
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
		////////LDT ó��////////////////////////////////
		// T�������Ϳ� ���ϴ� ��ġ�� �޸� ���� �־��ش�
		if(s_opcode.equals("74"))
		{	
			char[] value = new char[3];
			if(((rMgr.memory[rMgr.nowloc+1] >> 4) & 1) == 1) //4�����϶�
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
			else //3����
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
		///////// TD ó�� //////////////////////////////////
		// DEVICE�� �غ��� �� Ȯ���ϴ� �����̴�. �غ�Ǹ� PC�������Ϳ� 1 ����, �ȵǸ� 0�� �����Ѵ�.
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
		////////// JEQ ó��  ////////////////////////////////////
		//pc�������� ���� ���� �������� �׳� �Ѿ�� �����Ѵ�.
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
			
			if(rMgr.register[9] == 0) // �����Ƿ� operand �ּҷ� ����
			{	
				/// tooth compliment ���
				if(rMgr.getTA(rMgr.nowloc, 3)> 4000)
					rMgr.nowloc = rMgr.getTA(rMgr.nowloc, 3) +rMgr.nowloc + 3 - 4096;
				else
					rMgr.nowloc = rMgr.getTA(rMgr.nowloc, 3)+ rMgr.nowloc + 3;
			}
			else // �ٸ��Ƿ� ���� instruction���� �Ѿ��
			{
				rMgr.nowloc += 3;
			}
			return machinecode+"   JEQ";
		}
		/////////// RD ó�� ////////////////////////////////
		// Device���� �����ϳ��� �о A�������Ϳ� �����Ѵ�.
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
			rMgr.register[0] = rMgr.readDevice(devName); // A�������Ϳ� ���� ����Ÿ ����
			if(rMgr.register[0] == 48)  // 48�� ����Ǿ��ٴ� ���� 0�̶� ���ڸ� �о��ٴ� ���̹Ƿ� ���� 0���� ��� �־��ش�. 
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
		/////////// COMPR ó�� ///////////////////////////////////////
		// �������� 2���� ���� ���� ���ؼ� pc�������� ���� �����Ѵ�.
		if(s_opcode.equals("A0"))
		{
			if(rMgr.getRegister(rMgr.memory[rMgr.nowloc+1]>>4) == 
					rMgr.getRegister(rMgr.memory[rMgr.nowloc+1]&15))
			{
				rMgr.setRegister(9, 0); // ������ flag 0���� ����
			}
			else if(rMgr.getRegister(rMgr.memory[rMgr.nowloc+1]>>4) < 
					rMgr.getRegister(rMgr.memory[rMgr.nowloc+1]&15))
			{
				rMgr.setRegister(9, -1); // ���� ���� ������ flag -1���� ����
			}
			else if(rMgr.getRegister(rMgr.memory[rMgr.nowloc+1]>>4) == 
					rMgr.getRegister(rMgr.memory[rMgr.nowloc+1]&15))
			{
				rMgr.setRegister(9, 1); // ���� ���� ũ�� flag 1���� ����
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
		//////// STCH ó��  //////////////////////////////////////
		/// A�������Ϳ� �ִ� ���� �޸𸮿� �־��ش�
		if(s_opcode.equals("54"))
		{	
			int format4 = 0;
			if(((rMgr.memory[rMgr.nowloc+1] >> 4) & 1) == 1) // 4���� üũ
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
			if(((rMgr.memory[rMgr.nowloc+1] >> 4) & 8) == 8)// index üũ
				//���ϴ� �ּ��� index�� ���� ���� A�������� �� �־��ش�
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
		
		////////TIXR ó��  //////////////////////////////////////
		/// X�������� ���� ������Ų�� ���ϴ� �������� ���� ���ؼ� PC�������� ���� �����Ѵ�.
		if(s_opcode.equals("B8"))
		{	
			rMgr.register[1]++; // X�������� �� 1 ������Ų �� ��
			if( rMgr.getRegister(1)== rMgr.getRegister(rMgr.memory[rMgr.nowloc+1]>>4))
			{
				rMgr.setRegister(9, 0); // ������ flag 0���� ����
			}
			else if( rMgr.getRegister(1)< rMgr.getRegister(rMgr.memory[rMgr.nowloc+1]>>4))
			{
				rMgr.setRegister(9, -1); // X�� ������ flag -1���� ����
			}
			else if( rMgr.getRegister(1)> rMgr.getRegister(rMgr.memory[rMgr.nowloc+1]>>4))
			{
				rMgr.setRegister(9, 1); // X�� ũ�� flag 1���� ����
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
		//////////// JLT ó��   ///////////////////////////////////
		// PC�������Ͱ��� -1�Ͻ� ���ϴ� ������ ����
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
			
			if(rMgr.register[9] < 0) // FLAG ���� �����ÿ� TA�� �����Ѵ�.
			{	
				/// tooth compliment ���
				if(rMgr.getTA(rMgr.nowloc, 3)> 4000)
					rMgr.nowloc = rMgr.getTA(rMgr.nowloc, 3) +rMgr.nowloc + 3 - 4096;
				else
					rMgr.nowloc = rMgr.getTA(rMgr.nowloc, 3)+ rMgr.nowloc + 3;
			}
			else // ���� ���� ���� instruction���� �Ѿ��
			{
				rMgr.nowloc += 3;
			}
			return machinecode+"   JLT";
		}
		/////// STX ó��  ///////////////////////////////////
		/// X �������Ϳ� �ִ� ���� ���ϴ� �޸� �ּҿ� �־��ش�.
		if(s_opcode.equals("10"))
		{	
			int format4 = 0;
			if(((rMgr.memory[rMgr.nowloc+1] >> 4) & 1) == 1) // 4���� üũ
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
				// ���ϴ� �޸� �ּҿ� �������� ���� �־��ش�.
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
		//////// RSUB ó�� //////////////////////////
		// L�������Ϳ� ����� �ּҰ����� ���ư���.
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
			rMgr.nowloc = rMgr.getRegister(2); // L�������Ϳ� ����� ������ �����Ѵ�
			return machinecode+"   RSUB";
		}
		//////// LDA ó�� ///////////////////////////
		// ���ϴ� �ּ� �޸��� ���� A�������Ϳ� �־��ش�.
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
				//���ϴ� �ּ��� �޸𸮰��� A�������Ϳ� ����
				rMgr.setRegister(0, rMgr.byteToInt(value));
			}
			else if((rMgr.memory[rMgr.nowloc] & 3) == 1) // IMMEDIATE
			{
				TA = rMgr.getTA(rMgr.nowloc, 3);
				rMgr.setRegister(0, TA); // IMMEDIATE �� A�������Ϳ� ����
			}
			
			rMgr.nowloc += 3;
			return machinecode+"   LDA";
			
		}
		
		///////// COMP ó��  ////////////////////////////////
		///// A�������Ϳ� ���ϴ� ���� ���ؼ� PC�������Ϳ� ���� ����
		if(s_opcode.equals("28"))
		{
			if((rMgr.memory[rMgr.nowloc] & 3) == 1) // IMMEDIATE
			{
				TA = rMgr.getTA(rMgr.nowloc, 3);
				if( rMgr.getRegister(0)== TA)
				{
					rMgr.setRegister(9, 0); // ������ flag 0���� ����
				}
				else if( rMgr.getRegister(0)< TA)
				{
					rMgr.setRegister(9, -1); // X�� ������ flag -1���� ����
				}
				else if( rMgr.getRegister(0)> TA)
				{
					rMgr.setRegister(9, 1); // X�� ũ�� flag 1���� ����
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
		////// LDCH ó�� /////////////////////////
		///���ϴ� �ּ��� �޸𸮿��� �ѹ���Ʈ�� �о A�������Ϳ� �����Ѵ�. LDA�� �ٸ� ���� �ѹ���Ʈ�� �д� �ٴ� ���̴�.
		if(s_opcode.equals("50"))
		{	
			if(((rMgr.memory[rMgr.nowloc+1] >> 4) & 1) == 1) //4�����϶�
			{
				TA = rMgr.getTA(rMgr.nowloc, 4);
				rMgr.register[0] = rMgr.memory[TA+rMgr.getRegister(1)]; // �� �����ϴ� ����\
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
		
		////// WD ó��  /////////////////////////////
		// ����̽��� �غ� �Ǿ����� A�������Ϳ� ����� ���� write�Ѵ�.
		if(s_opcode.equals("DC"))
		{
			String devName = "";
			String s = "";
			char c;
			
			//�޸𸮿� ������ִ� device�� �̸��� ���ϴ� ����
			c = rMgr.memory[rMgr.getTA(rMgr.nowloc, 3) + rMgr.nowloc +3];
			s = String.format("%01X", c >> 4);
			devName +=s;
			s = String.format("%01X", c & 15);
			devName +=s;
			devName += ".txt";
			rMgr.writeDevice(devName,(char) rMgr.getRegister(0)); // A�������Ϳ� ����ִ� ���� device�� ����
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
		
		////// J ó��  /////////////////////////////
		/// ���ϴ� ��ġ�� �޸𸮷� �����Ѵ�.
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
			
			if((rMgr.memory[rMgr.nowloc] & 3) == 2) // INDIRECT ó��
			{	
				char[] value = new char[3];
				TA = rMgr.getTA(rMgr.nowloc, 3) + rMgr.nowloc + 3;
				value = rMgr.getMemory(TA, 3);
				rMgr.nowloc = rMgr.byteToInt(value);

			}
			else // DIRECT ó��
			{
				/// tooth compliment ���
				if(rMgr.getTA(rMgr.nowloc, 3)> 4000)
					rMgr.nowloc = rMgr.getTA(rMgr.nowloc, 3) +rMgr.nowloc + 3 - 4096;
				else
					rMgr.nowloc = rMgr.getTA(rMgr.nowloc, 3)+ rMgr.nowloc + 3;
			}
			return machinecode+"   J";
		}
		
		//////// STA ó�� ///////////////////////////
		/// A�������Ϳ� ����ִ� ���� ���ϴ� ��ġ�� �޸𸮿� �����Ѵ�.
		if(s_opcode.equals("0C"))
		{
			TA = rMgr.getTA(rMgr.nowloc, 3) + rMgr.nowloc + 3;
			for(i=0;i<3;i++)
			{
				rMgr.memory[TA+i] = rMgr.intToChar(rMgr.register[0])[i]; // �޸𸮿� A�������� �� �����Ѵ�.
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
