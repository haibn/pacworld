 /* Name: Bui Ngoc Hai
 * Class: K17 CS
 * Assignment 2: Packages World
 */
 /* Basic Strategy of Agent:
  * - see the percept (get information from visibleAgents, visiblePackage, messages....)
  * - if agent does not hold any package, choose a package to aimed 
  * - if agent see any new Package, Say to other Agents about all packages it known
  * - if agent has just Drop off or Pick up a package, Say to other Agents about this package
  * - if Agent hold a package, go to the package's destination (go reasonable) and Drop off
  * - if Agent does not hold any package, but aimed to a package, 
  *   go to the package's location (go reasonable)and Pick up
  * - if Agent does not hold any package, does not aim to any package,
  *    and does not know any package, go reasonable to look for packages 
 */

package BuiNgocHai;
import agent.*;
import pacworld.*;
import pacworld.Package;
import java.util.ArrayList;
import java.util.Random;

public class PacAgent extends Agent{
	
   private boolean bump; 
   private VisibleAgent[] visAgents;  //array of Visible Agents
   private VisiblePackage[] visPackages;  //array of Visible Packages
   private String[] messages;               //array of messages from other Agents
   private VisiblePackage heldPackage;      // heldPackage of this Agent
   private ArrayList<VisiblePackage> PacList;  //list of packages that Agent known
   private ArrayList<Action> actList;          //list of action that executed by this Agent
   private ArrayList<VisiblePackage> heldList;  // list of packages that held by any Agents
   private ArrayList<Coordinate> positionList;  // list of position of Agent in each turn
   private boolean hasNew;                      // hasNew==true if Agent see a any new package
   private int x;
   private int y;
   private int worldSize;
   private String status;
   private int idDroped;                       // id of the package that has just dropped by this agent
   private VisiblePackage aimedPackage;		   // the package that agent has aimed to pick up
   private int moveDir;						  // direction to move for looking for package when agent has no package to aim
   private int level;						// the level of agent, it's used to move for looking for package, 
   									      //level cang thap thi agent di chuyen cang gan duong bien
 //-----------------------------------------------------------  
   public PacAgent(){
   		super();
   		bump = false;
   		PacList = new ArrayList<VisiblePackage>();
   		actList = new ArrayList<Action>();
   		heldList = new ArrayList<VisiblePackage>();
   		positionList = new ArrayList<Coordinate>();
   		heldPackage = null;
   		hasNew = false;
   		x=-1;
   		y=-1;
   		idDroped=-1;
   		status = null;
   		aimedPackage = null;
   		worldSize = 50;
   		moveDir =-1;
   		level = 0;
   }
   
   public PacAgent(int id){
   		super(id);
   		bump = false;
   		PacList = new ArrayList<VisiblePackage>();
   		actList = new ArrayList<Action>();
   		heldList = new ArrayList<VisiblePackage>();
   		positionList = new ArrayList<Coordinate>();
   		heldPackage = null;
   		hasNew = false;
   		x=-1;
   		y=-1;
   		idDroped=-1;
   		status = null;
   		aimedPackage = null;
   		worldSize = 50;
   		moveDir = -1;
   		level = 9*(int)(id/2);
   }		
   /** Provide a Percept to the agent. This function is called by the
	   environment at the beginning of each agent's turn. If the agent has 
	   internal state, this method should also update it. */
   public void see(Percept p){
   	
            PacPercept p1 = (PacPercept)p;   		
   			bump = p1.feelBump();
   			visAgents = p1.getVisAgents().clone();
   			visPackages = p1.getVisPackages().clone();
   			messages = p1.getMessages().clone();
   			heldPackage = p1.getHeldPackage();
   			worldSize = p1.getWorldSize();
   			hasNew = false;
   			status = null;
   			//--------------------------
   			updateFromVision();
			updateFromMessages();
			//---------------------------------------
			positionList.add(new Coordinate(x,y));
			
			// neu da mang package thi khong de y den cai khac nua
			if(heldPackage!=null)aimedPackage = null;
			else{
				//neu chua con package muc tieu nao thi chon 1 package de di toi
				if(aimedPackage==null){
					int minDist = 2*worldSize;
					int index =-1;
					for(int i=0;i<PacList.size();i++)
						if(findInHeldList(PacList.get(i))==-1){
							int tmp = distance(PacList.get(i).getX(),PacList.get(i).getY());
								//+ distance(PacList.get(i).getX(),PacList.get(i).getY(),PacList.get(i).getDestX(),PacList.get(i).getDestY());
							if(tmp<minDist&& !(!actList.isEmpty() && 
								(actList.get(actList.size()-1)instanceof Dropoff) 
								&& idDroped==PacList.get(i).getId())){
								minDist = tmp;
								index = i;
							}
						}
					if(index>=0) 
						aimedPackage = PacList.get(index);
				}
			}
			//neu package dang nham toi da bi mang boi agent khac, thi khong nham toi no nua
			if(aimedPackage!=null)
				for(int i=0;i<visPackages.length;i++)
					if(visPackages[i].getId()==aimedPackage.getId() && visPackages[i].isHeld()==true)
					{	
						aimedPackage = null;
						break;
					}
			if(aimedPackage!=null) moveDir = -1;
			
   			
   }
   //------------------------------------------------------------------------
   public void updateFromVision(){
   	   // update new position of Agent
   	   for(int i=0;i<visAgents.length;i++){
   	   	   	 if(this.getId().equals(visAgents[i].getId()))
   	   	   	 {
   	   	   	  	x = visAgents[i].getX();
   	   	   	  	y = visAgents[i].getY();
   	   	   	 }	
   	   	   }

   	   //check visible packages and update new packages
   	   for(int i=0;i<visPackages.length;i++){
   	   	   if(findInPacList(visPackages[i])==-1)
   	   	   {
   	   	   	  PacList.add(visPackages[i]);
   	   	   	  if(visPackages[i].isHeld()==false)hasNew = true;
   	   	   }
   	   	   if(visPackages[i].isHeld()==true)
   	   	   	  if(findInHeldList(visPackages[i])==-1)
   	   	   	  	   heldList.add(visPackages[i]);
   	   }   	   
   }
   //---------------------------------------------------------------
   public void updateFromMessages(){
   	   
   	   // list de luu các package vua bi drop hoac held
   	   ArrayList<VisiblePackage> tmpList = new ArrayList<VisiblePackage>();
   	   
   	   for(int i=0; i < messages.length;i++)
   	   {
   			//-----------------
   			if(messages[i].startsWith("D"))
   			{
   				//update PacList and heldList about the package has just dropped
   				String tmp = messages[i].substring(1);
   				int idPacDroped = Integer.parseInt(tmp);
   				for(int j=0;j<PacList.size();j++)
   				{
   					if(PacList.get(j).getId()==idPacDroped){
   					   	tmpList.add(PacList.get(j));
   					   	PacList.remove(j);
   						break;
   					}
   				}
   				for(int j=0;j<heldList.size();j++)
   				{
   					if(heldList.get(j).getId()==idPacDroped){
   					   	heldList.remove(j);
   						break;
   					}
   				}
   			}//----------------------
   			else if(messages[i].startsWith("H"))
   			{
   				//update heldList when a package just held by an agent 
   				String tmp = messages[i].substring(1);
   				int idHeld= Integer.parseInt(tmp);
   				for(int j=0;j<PacList.size();j++)
   					if(PacList.get(j).getId()==idHeld)
   					{
   						tmpList.add(PacList.get(j));
   						if(findInHeldList(PacList.get(j))==-1)
   							heldList.add(PacList.get(j));
   						break;
   					}
   				//-----	
   				if(aimedPackage!=null)
   					if(idHeld==aimedPackage.getId())
   						aimedPackage = null;				
   			}
   			else
   			{	
   				//Xu lý các messages chua thông tin các package
   			    String[] ar = messages[i].split("-");
   				int tmpId=-1, tmpX=-1, tmpY=-1, tmpDestX=-1, tmpDestY=-1;
   				for(int j=0;j<ar.length;j++){
   					if(j%5==0)
   						tmpId=Integer.parseInt(ar[j]);
   					 else if(j%5==1)
   					 	  tmpX = Integer.parseInt(ar[j]);
   					 else if(j%5==2)
   					 	  tmpY = Integer.parseInt(ar[j]);
    				 else if(j%5==3)
   					 	  tmpDestX = Integer.parseInt(ar[j]);
   					 else if(j%5==4){
   					 	tmpDestY = Integer.parseInt(ar[j]);
   					 	Package pac = new Package(tmpId,-1,tmpDestX,tmpDestY);
   					 	pac.setX(tmpX);
   					 	pac.setY(tmpY);
   					 	VisiblePackage visPac = new VisiblePackage(pac);
   					 	// kiem tra xem PacList da có vispac chua 
   					 	int index =-1; 
   					 	for(int k=0;k<PacList.size();k++)
   					 		if(PacList.get(k).getId()==visPac.getId()){
   					 		    index = k;
   					 		    break;	
   					 		}
   					 	if(index==-1){
   					 		// kiem tra tmpList da có vispac chua
							int ind=-1;
							for(int k=0;k<tmpList.size();k++)
								if(tmpList.get(k).getId()==visPac.getId()){
									ind = k;
									break;
								}
							if(ind==-1)	
   					 			PacList.add(visPac);
   					 	}
   					 	tmpId=-1; tmpX=-1; tmpY=-1; tmpDestX=-1; tmpDestY=-1;		
   					 	
   					 }  					 	  	  
   				}
   			}
   			 
   			
   	   }
   	
   }
   //---------------------------------------------------
   //set Status to send to other Agents
   public void setStatus(){
   	   if(!actList.isEmpty() && (actList.get(actList.size()-1) instanceof Dropoff) && idDroped>=0){
   	   	  status="D";
   	   	  status+=idDroped;
   	   }
   	   else if(!actList.isEmpty() && (actList.get(actList.size()-1) instanceof Pickup)&& heldPackage!=null)
   	   {
   	   	  status="H";
   	   	  status+=heldPackage.getId();
   	   }
   	   else  // status = information of all package in it's PacList
   	   {
   	     status = "";
   	  	 for(int i=0;i<PacList.size();i++)
   	   	 {
   	   	 	
   	   	 	if(findInHeldList(PacList.get(i))==-1)
   	   	 	{
   	   	 		if(status.length()>0)status += "-";
   	   	 		status+=PacList.get(i).getId();
   	   	 		status+="-";
   	   	 		status+=PacList.get(i).getX();
   	     		status+="-";
   	   	 		status+=PacList.get(i).getY();
   	   	 		status+="-";
   	   	 		status+=PacList.get(i).getDestX();
   	   	 		status+="-";
   	   	 		status+=PacList.get(i).getDestY();
   	   	 	}
   	   	 }
   	   	 if(status.equals(""))status = null;
   	   }   	
   }
   //-----------------------
   // find a package in PacList
   public int findInPacList(VisiblePackage vpack){
   	   int index =-1;
   	   for(int i=0;i<PacList.size();i++)
   	   	 if(PacList.get(i).getId()==vpack.getId()){
   	   	 	 index = i;
   	   	 	 break;
   	   	 }
   	   return index;	 
   }
   //-------------------------
   // find a package in heldList
   public int findInHeldList(VisiblePackage vpack){
   	   int index =-1;
   	   for(int i=0;i<heldList.size();i++)
   	   	 if(heldList.get(i).getId()==vpack.getId()){
   	   	 	 index = i;
   	   	 	 break;
   	   	 }
   	   return index;	 
   }
   /** Have the agent select its next action to perform. Implements the
	   action0: Per -> Ac function or the action: I -> Ac function,
	   depending on whether or not the agent has internal state. Note,
	   Per (or I) is not passed as a parameter, because we assume this
	   information is recorded in an instance variable by the see() method. */
   public Action selectAction(){
   		
   		Action act = new Idle();

   		if(idDroped>=0 && !actList.isEmpty() && (actList.get(actList.size()-1) instanceof Dropoff))
   		{  // if the last action is Dropoff
   			setStatus();
   			if(status!=null);
   				act = new Say(status);		
   		}
   		else if(heldPackage!=null && !actList.isEmpty() && (actList.get(actList.size()-1) instanceof Pickup))
   		{   //if the last action is Pickup
   			setStatus();
   			if(status!=null);
   				act = new Say(status);   		
   		}
   		else if(hasNew==true)
   		{   //if Agent see new Packages
   			setStatus();
   			if(status!=null);
   				act = new Say(status);
   		}
   		else if(heldPackage!=null){
   			// bring package to destination
   			if(heldPackage.getDestX()==x && heldPackage.getDestY()==y-1)
   			{
   				idDroped = heldPackage.getId();
   				boolean drop = true;  // check xem có the drop duoc không
   				for(int i=0;i<visPackages.length;i++)
   					if(visPackages[i].getX()==heldPackage.getDestX()
   						&& visPackages[i].getY()==heldPackage.getDestY()
   						&& visPackages[i].getId()!=heldPackage.getId())
   						{
   							drop = false;
   							break;
   						}
   				if(drop==true)
   					act = new Dropoff(Direction.NORTH);
   				else if(heldPackage.getX()==x-1)
   					act = new Dropoff(Direction.WEST);
   				else if(heldPackage.getX()==x+1)
   					act = new Dropoff(Direction.EAST);
   				else
   					act = new Dropoff(Direction.SOUTH); 				
   			}
   			else if(heldPackage.getDestX()==x && heldPackage.getDestY()==y+1)
   			{
   				idDroped = heldPackage.getId();
   				boolean drop = true;// check xem có the drop duoc không
   				for(int i=0;i<visPackages.length;i++)
   					if(visPackages[i].getX()==heldPackage.getDestX()
   						&& visPackages[i].getY()==heldPackage.getDestY()
   							&& visPackages[i].getId()!=heldPackage.getId())
   						{
   							drop = false;
   							break;
   						}
   				if(drop==true)
   				   act = new Dropoff(Direction.SOUTH);
   				else if(heldPackage.getX()==x-1)
   					act = new Dropoff(Direction.WEST);
   				else if(heldPackage.getX()==x+1)
   					act = new Dropoff(Direction.EAST);
   				else
   					act = new Dropoff(Direction.NORTH); 
   			}
    		else if(heldPackage.getDestX()==x-1 && heldPackage.getDestY()==y)
   			{
   				idDroped = heldPackage.getId();
   				boolean drop = true;// check xem có the drop duoc không
   				for(int i=0;i<visPackages.length;i++)
   					if(visPackages[i].getX()==heldPackage.getDestX()
   						&& visPackages[i].getY()==heldPackage.getDestY()
   							&& visPackages[i].getId()!=heldPackage.getId())
   						{
   							drop = false;
   							break;
   						}
   				if(drop==true)
   					act = new Dropoff(Direction.WEST);
   			    else if(heldPackage.getY()==y-1)
   					act = new Dropoff(Direction.NORTH);
   				else if(heldPackage.getY()==y+1)
   					act = new Dropoff(Direction.SOUTH);
   				else
   					act = new Dropoff(Direction.EAST); 
   			}
   			else if(heldPackage.getDestX()==x+1 && heldPackage.getDestY()==y)
   			{
   				idDroped = heldPackage.getId();
   				boolean drop = true;// check xem có the drop duoc không
   				for(int i=0;i<visPackages.length;i++)
   					if(visPackages[i].getX()==heldPackage.getDestX()
   						&& visPackages[i].getY()==heldPackage.getDestY()
   							&& visPackages[i].getId()!=heldPackage.getId())
   						{
   							drop = false;
   							break;
   						}
   				if(drop==true)
   				   act = new Dropoff(Direction.EAST);
   			    else if(heldPackage.getY()==y-1)
   					act = new Dropoff(Direction.NORTH);
   				else if(heldPackage.getY()==y+1)
   					act = new Dropoff(Direction.SOUTH);
   				else
   					act = new Dropoff(Direction.WEST); 
   			}
   			else		  					
   			    act = goToDest();  // go to destination of Package
   		}
   		else if(aimedPackage!=null){
   			//pickup if agent beside the aimed package
   			if(aimedPackage.getX()==x && aimedPackage.getY()==y-1)
   			{
   				act = new Pickup(Direction.NORTH);
   			}
   			else if(aimedPackage.getX()==x && aimedPackage.getY()==y+1)
   			{
   				act = new Pickup(Direction.SOUTH);
   			}
    		else if(aimedPackage.getX()==x-1 && aimedPackage.getY()==y)
   			{
   				act = new Pickup(Direction.WEST);
   			}
   			else if(aimedPackage.getX()==x+1 && aimedPackage.getY()==y)
   			{
   				act = new Pickup(Direction.EAST);
   			}
   			else
   			act = goToDest();  // go to the aimed package
   		}
   		else // aimedPackage == null, di mò package
   		{

   			if(moveDir==Direction.NORTH && x==4+level && y==worldSize-4-level)
   				level = level+9;
   			if(level>worldSize/2-8)
   				level = 0;
   					
   			if(moveDir==-1)
   				   moveDir = Direction.WEST;  //dau tien la di sang trai
   			if(x>4+level && moveDir==Direction.WEST)
   			{
   				if(checkBump(Direction.WEST)==0)
   				   act = new Move(Direction.WEST);
   				else if(checkBump(Direction.NORTH)==0)
   				   act = new Move(Direction.NORTH);   
   			}
   			if(x<=4+level && y>4+level && moveDir==Direction.WEST)  //doi huong len tren
   				moveDir = Direction.NORTH;  
   			if(x<=4+level && y<=4+level && moveDir==Direction.WEST)  //doi huong sang phai
   				moveDir = Direction.EAST;
   			if(y>4+level && moveDir==Direction.NORTH)
   			{
   				if(checkBump(Direction.NORTH)==0)
   				   act = new Move(Direction.NORTH);
   				else if(checkBump(Direction.EAST)==0)
   				   act = new Move(Direction.EAST);
   		     
   			}
   			if(y<=4+level && moveDir==Direction.NORTH)  //doi huong sang phai
   				moveDir = Direction.EAST;
   			if(x<worldSize-4-level && moveDir==Direction.EAST)
   			{
   				if(checkBump(Direction.EAST)==0)
   				   act = new Move(Direction.EAST);
   				else if(checkBump(Direction.SOUTH)==0)
   				   act = new Move(Direction.SOUTH);
   			     
   			}
   			if(x>=worldSize-4-level&& moveDir==Direction.EAST)//doi huong xuong duoi
   				moveDir = Direction.SOUTH;
   			if(y<=worldSize-4-level && moveDir==Direction.SOUTH)
   			{
   				if(checkBump(Direction.SOUTH)==0)
   				   act = new Move(Direction.SOUTH);
   				else if(checkBump(Direction.WEST)==0)
   				   act = new Move(Direction.WEST);
   		      
   			}
   			if(y>worldSize-4-level && moveDir==Direction.SOUTH)  //doi huong sang trai
   				moveDir = Direction.WEST;
   			if(x>4+level && moveDir==Direction.WEST)
   			{
   				if(checkBump(Direction.WEST)==0)
   				   act = new Move(Direction.WEST);
   				else if(checkBump(Direction.NORTH)==0)
   				   act = new Move(Direction.NORTH);
   			}
   		}	 		
   		
   		actList.add(act);
   		return act;
   }
   //choose action for go to a package or destination of a package
   public Action goToDest(){
   	    if(heldPackage!=null){ //neu dang mang package, di den dich
   	    	int destX,destY;
   	    	destX = heldPackage.getDestX();
   	    	destY = heldPackage.getDestY();
   	    	if(Math.abs(this.x-destX)>=Math.abs(this.y-destY)) //distance x > distance y
   	    	{
   	    		if(this.x>destX)
   	    		{
   	    		  
   	    		   if(checkBump(Direction.WEST)==0)
   	    				  return new Move(Direction.WEST);
   	    		   else if(positionList.size()>=3 && 
   	    		   	positionList.get(positionList.size()-1).equals(positionList.get(positionList.size()-3))
   	    		   	&& actList.size()>=3 && (actList.get(actList.size()-1)instanceof Move)
   	    		   	&& (actList.get(actList.size()-2)instanceof Move)&& (actList.get(actList.size()-3)instanceof Move))
   	    		   	{
   	    		   		if(positionList.get(positionList.size()-1).y==positionList.get(positionList.size()-2).y+1)
   	    		   		{     if(checkBump(Direction.SOUTH)==0)
   	    		   	     		return new Move(Direction.SOUTH);
   	    		   	     	  else return new Idle();
   	    		   		}
   	    		   		else if(positionList.get(positionList.size()-1).y==positionList.get(positionList.size()-2).y-1)
   	    		   		{     if(checkBump(Direction.NORTH)==0)
   	    		   	     		return new Move(Direction.NORTH);
   	    		   	     	  else return new Idle();
   	    		   		}
   	    		   		else return new Idle();	
   	    		   }	
   	    		   else if(checkBump(Direction.WEST)==2)
   	    		   {
   	    		   	     if(heldPackage.getY()==y+1)
   	    		   	     {
   	    		   	     	if(checkBump(Direction.SOUTH)==0)
   	    		   	     		return new Move(Direction.SOUTH);
   	    		   	     	else if(checkBump(Direction.EAST)==0)
   	    		   	     		return new Move(Direction.EAST);
   	    		   	     	else return new Idle();
   	    		   	     }
   	    		   	     else  //PacY == y-1
   	    		   	     {
   	    		   	        if(checkBump(Direction.NORTH)==0)
   	    		   	     		return new Move(Direction.NORTH);
   	    		   	     	else if(checkBump(Direction.EAST)==0)
   	    		   	     		return new Move(Direction.EAST);
   	    		   	     	else return new Idle();	
   	    		   	     }	    
   	    		   }
   	    		   else if(checkBump(Direction.WEST)==3)
   	    		   {
   	    		   	     if(heldPackage.getY()==y-1)
   	    		   	     {
   	    		   	     	if(checkBump(Direction.SOUTH)==0)
   	    		   	     		return new Move(Direction.SOUTH);
   	    		   	     	else if(checkBump(Direction.EAST)==0)
   	    		   	     		return new Move(Direction.EAST);
   	    		   	     	else return new Idle();
   	    		   	     }
   	    		   	     else  //PacY == y+1
   	    		   	     {
   	    		   	        if(checkBump(Direction.NORTH)==0)
   	    		   	     		return new Move(Direction.NORTH);
   	    		   	     	else if(checkBump(Direction.EAST)==0)
   	    		   	     		return new Move(Direction.EAST);
   	    		   	     	else return new Idle();	
   	    		   	     }	    
   	    		   }
   	    		   else if(checkBump(Direction.NORTH)==0)
   	    		   		 return new Move(Direction.NORTH);
   	    		   else return new Idle();
   	    		}
   	    		else  // x <= destX
   	    		{
   	    		  
   	    		   if(checkBump(Direction.EAST)==0)
   	    				  return new Move(Direction.EAST);
   	    		   else if(positionList.size()>=3 && 
   	    		   	positionList.get(positionList.size()-1).equals(positionList.get(positionList.size()-3))
   	    		   	&& actList.size()>=3 && (actList.get(actList.size()-1)instanceof Move)
   	    		   	&& (actList.get(actList.size()-2)instanceof Move)&& (actList.get(actList.size()-3)instanceof Move))
   	    		   	{
   	    		   		if(positionList.get(positionList.size()-1).y==positionList.get(positionList.size()-2).y+1)
   	    		   		{     if(checkBump(Direction.SOUTH)==0)
   	    		   	     		return new Move(Direction.SOUTH);
   	    		   	     	  else return new Idle();
   	    		   		}
   	    		   		else if(positionList.get(positionList.size()-1).y==positionList.get(positionList.size()-2).y-1)
   	    		   		{     if(checkBump(Direction.NORTH)==0)
   	    		   	     		return new Move(Direction.NORTH);
   	    		   	     	  else return new Idle();
   	    		   		}
   	    		   		else return new Idle();	
   	    		   }	  
   	    		   else if(checkBump(Direction.EAST)==2)
   	    		   {
   	    		   	     if(heldPackage.getY()==y+1)
   	    		   	     {
   	    		   	     	if(checkBump(Direction.SOUTH)==0)
   	    		   	     		return new Move(Direction.SOUTH);
   	    		   	     	else if(checkBump(Direction.WEST)==0)
   	    		   	     		return new Move(Direction.WEST);
   	    		   	     	else return new Idle();
   	    		   	     }
   	    		   	     else  //PacY == y-1
   	    		   	     {
   	    		   	        if(checkBump(Direction.NORTH)==0)
   	    		   	     		return new Move(Direction.NORTH);
   	    		   	     	else if(checkBump(Direction.WEST)==0)
   	    		   	     		return new Move(Direction.WEST);
   	    		   	     	else return new Idle();	
   	    		   	     }	    
   	    		   }
   	    		   else if(checkBump(Direction.EAST)==3)
   	    		   {
   	    		   	     if(heldPackage.getY()==y-1)
   	    		   	     {
   	    		   	     	if(checkBump(Direction.SOUTH)==0)
   	    		   	     		return new Move(Direction.SOUTH);
   	    		   	     	else if(checkBump(Direction.WEST)==0)
   	    		   	     		return new Move(Direction.WEST);
   	    		   	     	else return new Idle();
   	    		   	     }
   	    		   	     else  //PacY == y+1
   	    		   	     {
   	    		   	        if(checkBump(Direction.NORTH)==0)
   	    		   	     		return new Move(Direction.NORTH);
   	    		   	     	else if(checkBump(Direction.WEST)==0)
   	    		   	     		return new Move(Direction.WEST);
   	    		   	     	else return new Idle();	
   	    		   	     }	    
   	    		   }	  
   	    		   else if(checkBump(Direction.SOUTH)==0)
   	    		   		 return new Move(Direction.SOUTH);
   	    		   else return new Idle(); 
   	    		}
   	    	}
   	    	else //Math.abs(this.x-destX) < Math.abs(this.y-destY)
   	    	{
   	    		if(this.y>destY)
   	    		{
   	    			
   	    			if(checkBump(Direction.NORTH)==0)
   	    				return new Move(Direction.NORTH);
					else if(positionList.size()>=3 && 
   	    		   	positionList.get(positionList.size()-1).equals(positionList.get(positionList.size()-3))
   	    		   	&& actList.size()>=3 && (actList.get(actList.size()-1)instanceof Move)
   	    		   	&& (actList.get(actList.size()-2)instanceof Move)&& (actList.get(actList.size()-3)instanceof Move))
   	    		   	{
   	    		   		if(positionList.get(positionList.size()-1).x==positionList.get(positionList.size()-2).x+1)
   	    		   		{     if(checkBump(Direction.EAST)==0)
   	    		   	     		return new Move(Direction.EAST);
   	    		   	     	  else return new Idle();
   	    		   		}
   	    		   		else if(positionList.get(positionList.size()-1).x==positionList.get(positionList.size()-2).x-1)
   	    		   		{     if(checkBump(Direction.WEST)==0)
   	    		   	     		return new Move(Direction.WEST);
   	    		   	     	  else return new Idle();
   	    		   		}
   	    		   		else return new Idle();	
   	    		   }
   	    		   else if(checkBump(Direction.NORTH)==2)
   	    		   {
   	    		   	     if(heldPackage.getX()==x+1)
   	    		   	     {
   	    		   	     	if(checkBump(Direction.EAST)==0)
   	    		   	     		return new Move(Direction.EAST);
   	    		   	     	else if(checkBump(Direction.SOUTH)==0)
   	    		   	     		return new Move(Direction.SOUTH);
   	    		   	     	else return new Idle();
   	    		   	     }
   	    		   	     else  //PacX == x-1
   	    		   	     {
   	    		   	        if(checkBump(Direction.WEST)==0)
   	    		   	     		return new Move(Direction.WEST);
   	    		   	     	else if(checkBump(Direction.SOUTH)==0)
   	    		   	     		return new Move(Direction.SOUTH);
   	    		   	     	else return new Idle();	
   	    		   	     }	    
   	    		   }
   	    		   else if(checkBump(Direction.NORTH)==3)
   	    		   {
   	    		   	     if(heldPackage.getX()==x-1)
   	    		   	     {
   	    		   	     	if(checkBump(Direction.EAST)==0)
   	    		   	     		return new Move(Direction.EAST);
   	    		   	     	else if(checkBump(Direction.SOUTH)==0)
   	    		   	     		return new Move(Direction.SOUTH);
   	    		   	     	else return new Idle();
   	    		   	     }
   	    		   	     else  //PacX == X+1
   	    		   	     {
   	    		   	        if(checkBump(Direction.WEST)==0)
   	    		   	     		return new Move(Direction.WEST);
   	    		   	     	else if(checkBump(Direction.SOUTH)==0)
   	    		   	     		return new Move(Direction.SOUTH);
   	    		   	     	else return new Idle();	
   	    		   	     }	    
   	    		   }
   	    			else if(checkBump(Direction.EAST)==0)
   	    				return new Move(Direction.EAST);
   	    			else return new Idle();		
   	    		}
   	    		else  //y <= destY
   	    		{
   	    			
   	    			if(checkBump(Direction.SOUTH)==0)
   	    				return new Move(Direction.SOUTH);
   	    		   else if(positionList.size()>=3 && 
   	    		   	positionList.get(positionList.size()-1).equals(positionList.get(positionList.size()-3))
   	    		   	&& actList.size()>=3 && (actList.get(actList.size()-1)instanceof Move)
   	    		   	&& (actList.get(actList.size()-2)instanceof Move)&& (actList.get(actList.size()-3)instanceof Move))
   	    		   	{
   	    		   		if(positionList.get(positionList.size()-1).x==positionList.get(positionList.size()-2).x+1)
   	    		   		{     if(checkBump(Direction.EAST)==0)
   	    		   	     		return new Move(Direction.EAST);
   	    		   	     	  else return new Idle();
   	    		   		}
   	    		   		else if(positionList.get(positionList.size()-1).x==positionList.get(positionList.size()-2).x-1)
   	    		   		{     if(checkBump(Direction.WEST)==0)
   	    		   	     		return new Move(Direction.WEST);
   	    		   	     	  else return new Idle();
   	    		   		}
   	    		   		else return new Idle();	
   	    		   }
   	    		   else if(checkBump(Direction.SOUTH)==2)
   	    		   {
   	    		   	     if(heldPackage.getX()==x+1)
   	    		   	     {
   	    		   	     	if(checkBump(Direction.EAST)==0)
   	    		   	     		return new Move(Direction.EAST);
   	    		   	     	else if(checkBump(Direction.NORTH)==0)
   	    		   	     		return new Move(Direction.NORTH);
   	    		   	     	else return new Idle();
   	    		   	     }
   	    		   	     else  //PacX == x-1
   	    		   	     {
   	    		   	        if(checkBump(Direction.WEST)==0)
   	    		   	     		return new Move(Direction.WEST);
   	    		   	     	else if(checkBump(Direction.NORTH)==0)
   	    		   	     		return new Move(Direction.NORTH);
   	    		   	     	else return new Idle();	
   	    		   	     }	    
   	    		   }
   	    		   else if(checkBump(Direction.SOUTH)==3)
   	    		   {
   	    		   	     if(heldPackage.getX()==x-1)
   	    		   	     {
   	    		   	     	if(checkBump(Direction.EAST)==0)
   	    		   	     		return new Move(Direction.EAST);
   	    		   	     	else if(checkBump(Direction.NORTH)==0)
   	    		   	     		return new Move(Direction.NORTH);
   	    		   	     	else return new Idle();
   	    		   	     }
   	    		   	     else  //PacX == X+1
   	    		   	     {
   	    		   	        if(checkBump(Direction.WEST)==0)
   	    		   	     		return new Move(Direction.WEST);
   	    		   	     	else if(checkBump(Direction.NORTH)==0)
   	    		   	     		return new Move(Direction.NORTH);
   	    		   	     	else return new Idle();	
   	    		   	     }	    
   	    		   }
   	    			else if(checkBump(Direction.WEST)==0)
   	    				return new Move(Direction.WEST);
   	    			else return new Idle();  			
   	    		}
   	    	}
   	    }
   	    else if(aimedPackage!=null) // neu dang nham den 1 package, se di chuyen den vi tri cua package do
   	    {
   	    	int aimedX,aimedY;
   	    	aimedX = aimedPackage.getX();
   	    	aimedY = aimedPackage.getY();
   	    	if(Math.abs(this.x-aimedX)>=Math.abs(this.y-aimedY)) //distance x > distance y
   	    	{
   	    		if(this.x>aimedX)
   	    		{
   	    		   
   	    		   if(checkBump(Direction.WEST)==0)
   	    				  return new Move(Direction.WEST);
   	    		   else if(checkBump(Direction.NORTH)==0)
   	    		   		 return new Move(Direction.NORTH);
   	    		   else return new Idle();
   	    		}
   	    		else  // x <= aimedX
   	    		{
   	    		   if(checkBump(Direction.EAST)==0)
   	    				  return new Move(Direction.EAST);
   	    		   else if(checkBump(Direction.SOUTH)==0)
   	    		   		 return new Move(Direction.SOUTH);
   	    		   else return new Idle();
   	    		}

   	    	}
   	    	else  // distance y > distance x 
   	    	{
   	    		if(this.y>aimedY)
   	    		{
   	    			
   	    			if(checkBump(Direction.NORTH)==0)
   	    				return new Move(Direction.NORTH);
   	    			else if(checkBump(Direction.EAST)==0)
   	    				return new Move(Direction.EAST);
   	    			else return new Idle();	    			
   	    		}
   	    		else  //y <= aimedY
   	    		{
   	    			if(checkBump(Direction.SOUTH)==0)
   	    				return new Move(Direction.SOUTH);
   	    			else if(checkBump(Direction.WEST)==0)
   	    				return new Move(Direction.WEST);
   	    			else return new Idle();	    			
   	    		}
   	    	}	
   	    }
   	    else
   	    return new Idle();
   }
   //----------------------
   // check xem agent có bump không neu di theo huong direction
   //ham nay tra ve 1,2,3 la cac kieu bump khac nhau, tra ve 0 neu khong bump
   public int checkBump(int direction){
   	   
   	   if(heldPackage!=null)
   	   {
   	   	  int newX,newY,newPacX,newPacY;
   	   	  newX = x + Direction.DELTA_X[direction];
   	   	  newY = y + Direction.DELTA_Y[direction];
   	   	  newPacX = heldPackage.getX() + Direction.DELTA_X[direction];
   	   	  newPacY = heldPackage.getY() + Direction.DELTA_Y[direction];
   	   	  
   	   	  if(newPacX==x && newPacY==y)//vi tri moi cua package là vi trí hien tai cua Agent
   	   	  {
   	   	  	   if(newX<0 || newX>=worldSize || newY<0 || newY>=worldSize ) 
   	   	  			return 1;
   	   	  	   for(int i=0;i<visPackages.length;i++)
				  if(visPackages[i].getX()==newX && visPackages[i].getY()==newY)
					 return 1;
				
   	   	  	   for(int i=0;i<visAgents.length;i++)
				  if(visAgents[i].getX()==newX && visAgents[i].getY()==newY)
				     	return 1;
   	   	  }
   	   	  else if(newX==heldPackage.getX() && newY==heldPackage.getY())//vi tri moi cua Agent là vi trí hien tai cua Package
   	   	  {
   	   	  	   if(newPacX<0 || newPacX>=worldSize || newPacY<0 || newPacY>=worldSize ) 
   	   	  			return 1;
   	   	  	   for(int i=0;i<visPackages.length;i++)
				  if(visPackages[i].getX()==newPacX && visPackages[i].getY()==newPacY)
					 return 1;
				
   	   	  	   for(int i=0;i<visAgents.length;i++)
				  if(visAgents[i].getX()==newPacX && visAgents[i].getY()==newPacY)
				     	return 1;
   	   	  }
   	   	  else  //di chuyen song song
   	   	  {
   	   	  	   if(newPacX<0 || newPacX>=worldSize || newPacY<0 || newPacY>=worldSize ||
   	   	  	   	newX<0 || newX>=worldSize || newY<0 || newY>=worldSize) 
   	   	  			return 1;
   	   	  	   for(int i=0;i<visPackages.length;i++)
   	   	  	   {
				  if(visPackages[i].getX()==newX && visPackages[i].getY()==newY)
					 	return 2;	
   	   	  	   }
   	   	  	   for(int i=0;i<visAgents.length;i++)
   	   	  	   {
				  if(visAgents[i].getX()==newX && visAgents[i].getY()==newY)
				     	return 2;  		  
   	   	  	   }
   	   	  	   for(int i=0;i<visPackages.length;i++)
   	   	  	   {
				  if(visPackages[i].getX()==newPacX && visPackages[i].getY()==newPacY)
				  		return 3;	
   	   	  	   }
   	   	  	   for(int i=0;i<visAgents.length;i++)
   	   	  	   { 
				  if(visAgents[i].getX()==newPacX && visAgents[i].getY()==newPacY)
				  	   return 3;		  
   	   	  	   } 	   	  	
   	   	  	
   	   	  }
   	   }
   	   else // if heldPackage ==null
   	   {
   	   	  int newX,newY;
   	   	  newX = x + Direction.DELTA_X[direction];
   	   	  newY = y + Direction.DELTA_Y[direction];
   	   	  
   	   	  if(newX<0 || newX>=worldSize || newY<0 || newY>=worldSize ) 
   	   	  	return 1;
   	   	  	
   	   	  for(int i=0;i<visPackages.length;i++)
				if(visPackages[i].getX()==newX && visPackages[i].getY()==newY)
					return 1;
				
   	   	  for(int i=0;i<visAgents.length;i++)
				if(visAgents[i].getX()==newX && visAgents[i].getY()==newY)
					return 1;
   	   }
   	   return 0;
   }
   //-------------------
   public int distance(int x, int y){
   	  return (Math.abs(this.x - x) + Math.abs(this.y - y));
   }
      //-------------------
   public int distance(int x1, int y1, int x2, int y2){
   	  return (Math.abs(x1 - x2) + Math.abs(y1 - y2));
   }
   /** Return a unique string that identifies the agent. This is particularly
       useful in multi-agent environments. */
   public String getId() {
   	return id;
   }      
}
//----------------------------------------------------------------------------
// coordinate of agent
class Coordinate{
	
	public int x;
	public int y;
	
	public Coordinate(){
		x=0;
		y=0;
	}
	public Coordinate(int x, int y){
		this.x=x;
		this.y=y;
	}
	public Coordinate(Coordinate c){
		this.x=c.x;
		this.y=c.y;
	}
	
	public boolean equals(Object o) {
		if (!(o instanceof Coordinate))
			return false;
		Coordinate c = (Coordinate)o;
		if (c.x == x && c.y == y)
			return true;
		else
			return false;
	}
}



