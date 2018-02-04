import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;
import java.util.Map.Entry;

public class ItemCounter {

	HashMap<Integer,Ride> ridesMap = new HashMap<Integer,Ride>();
	ArrayList<TimeAndID> startList= new ArrayList<TimeAndID>();
	ArrayList<TimeAndID> endList = new ArrayList<TimeAndID>();
	ArrayList<TimeAndID> allTimeStamps = new ArrayList<TimeAndID>();
	HashMap<Integer,String> itemsMap = new HashMap<Integer,String>();
	HashMap<String,Integer> aggregatedMap = new HashMap<String,Integer>();

	TimeAndID startObj, endObj; // Using a custom data structure to store the time slot start time and associated ride ID to know basket contents in every time slot
	Ride ride;
	int ctr=0;

	public void process_ride(Ride ride){
		ridesMap.put(ridesMap.size(), ride); // Storing a HashMap with id (auto increment) and The Ride object. Doing this because makes referencing the ID from the TimeAndID object fairly easy
	}

	public void print_items_per_interval(){

		for(int i=0; i<ridesMap.size(); i++){
			ride = ridesMap.get(i);
			startObj = new TimeAndID(ride.getStart(),i); // Creating the TimeAndID object that has the start time of the ride and the ride ID
			endObj = new TimeAndID(ride.getEnd(),i); // Creating the TimeAndID object that has the end time of the ride and the ride ID
			startList.add(startObj); // Creating a list of all such TimeAndID objects
			endList.add(endObj);
		}

		Collections.sort(startList); // Sorting these lists because we need the slots in ascending order to examine the basket contents for every possible slot
		Collections.sort(endList); // Time complexity is nlogn - Mergesort is used here under the hood
		allTimeStamps.addAll(startList);
		allTimeStamps.addAll(endList);
		Collections.sort(allTimeStamps);

		int i=0, j=0, id;
		Date prev = startList.get(0).getDate();
		while(i<startList.size() && j<endList.size()){ // Going over every Timestamp possible and then examining all the rides in the slot under consideration and their basket contents and aggregating them in a map so that we can get a count of all the items on there
			if((startList.get(i).getDate()).before(endList.get(j).getDate())){
				id = startList.get(i).getID();
				itemsMap.put(id,ridesMap.get(id).getItems());
				aggregateItems(itemsMap);
				prev = startList.get(i).getDate();
				i++;
			}
			else if((startList.get(i).getDate()).after(endList.get(j).getDate())){
				id = endList.get(j).getID();
				itemsMap.remove(id);
				aggregateItems(itemsMap);
				j++;
			}
		}
		while(i<startList.size()){ // Doing the same for all the remaining time stamps in the start list
			id = startList.get(i).getID();
			itemsMap.put(id,ridesMap.get(id).getItems());
			aggregateItems(itemsMap);
			i++;

		}
		while(j<endList.size()){ // Doing the same for all the remaining time stamps in the end list
			id = endList.get(j).getID();
			itemsMap.remove(id);
			aggregateItems(itemsMap);
			j++;
		}

	}

	public void aggregateItems(HashMap<Integer, String> itemsMap){ // A function that aggregates all the items in a particular time slot to give a total count of all the contents of the basket
		  Set temp=itemsMap.entrySet();
	      Iterator it=temp.iterator();
	      HashMap<String, Integer> map = new HashMap<String,Integer>();
	      String key;
	      int val;

	      while(it.hasNext()){
	    	  Map.Entry<Integer,String> entry=(Entry<Integer,String>) it.next();
	    	  String items[] = entry.getValue().split(",\\s+");
	    	  for(int i=0; i<items.length; i++){
	    		  key = items[i].split("\\s+")[1];
	    		  val = Integer.parseInt(items[i].split("\\s+")[0]);
	    		  if(map.containsKey(key))
	    			  map.put(key,map.get(key)+val);
	    		  else
	    			  map.put(key,val);
	    	  }
	      }

	      //This bit is responsible for the printing of the time stamps on the left hand side of the basket contents in the output
	      if(ctr<allTimeStamps.size()-1)
	    	  System.out.println(allTimeStamps.get(ctr).stringVal()+"->"+allTimeStamps.get(ctr+1).stringVal()+"\t"+map.toString());
	      else
	    	  System.out.println(allTimeStamps.get(ctr).stringVal()+"-> Infinity \t"+map.toString());
	      ctr++;

	}

	public static void main(String[] args) throws ParseException, Exception {

		/*Ride r1 = new Ride("7:00","7:30","2 Apple, 1 Brownie"); //Creating the ride objects
		Ride r2 = new Ride("7:10","8:00","1 Apple, 3 Carrots"); //Item Input has to have a space after Comma. That's how string parsing has been taken care of
		Ride r3 = new Ride("7:15","7:19","1 Apple, 2 Brownie, 4 Diamonds");
		Ride r4 = new Ride("7:20","7:45","1 Apple, 2 Brownie, 4 Diamonds");
		*/

		Ride r1 = new Ride("6:13","6:23","2 Apple, 1 Brownie"); //Creating the ride objects
		Ride r2 = new Ride("5:50","6:08","1 Apple, 3 Carrots"); //Item Input has to have a space after Comma. That's how string parsing has been taken care of
		Ride r3 = new Ride("6:10","6:30","1 Apple, 2 Brownie, 4 Diamonds");
		Ride r4 = new Ride("5:52","6:33","3 Carrots, 1 Brownie, 4 Apple");
		Ride r5 = new Ride("5:58","6:23","1 Apple");


		ItemCounter counter = new ItemCounter();
		counter.process_ride(r1);
		counter.process_ride(r2);
		counter.process_ride(r3);
		counter.process_ride(r4);
		counter.process_ride(r5);

		counter.print_items_per_interval();
	}

}

class Ride{

	private String items;
	private Date start, end;
	DateFormat sdf = new SimpleDateFormat("hh:mm");

	public Ride(String start, String end, String items) throws ParseException, Exception{
		this.start = sdf.parse(start); //Storing the string times as a Date object so comparison is easy
		this.end = sdf.parse(end);
		if(!this.start.before(this.end)) // Handling the case where ride start time is after ride end time
			throw new Exception("Invalid Input. Please enter start and end time correctly");
		this.items = items;
	}
	public void stringVal(){
		System.out.println("\tStart: "+start+" End: "+end+" Items: "+items);
	}
	public Date getStart(){
		return this.start;
	}
	public Date getEnd(){
		return this.end;
	}
	public String getItems(){
		return this.items;
	}

}

class TimeAndID implements Comparable<TimeAndID>{ // Using a custom data structure to store the time slot start time and associated ride ID to know basket contents in every time slot

	private Date date;
	private int id;

	TimeAndID(Date date, int id){
		this.date = date;
		this.id = id;
	}
	public Date getDate(){
		return this.date;
	}
	public int getID(){
		return this.id;
	}
	public String stringVal(){
		return date.getHours()+":"+date.getMinutes();
	}
	@Override
	public int compareTo(TimeAndID o) { //Comparator to get the timestamps in ascending order
		if(this.date.before(o.getDate()))
			return -1;
		else if(this.date.after(o.getDate()))
			return 1;
		else
			return 0;
	}
}
