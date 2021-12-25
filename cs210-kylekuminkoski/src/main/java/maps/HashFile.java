package maps;

import java.lang.reflect.Array;
import java.nio.file.Path;
import java.util.Iterator;
import java.util.List;

import types.Entry;
import types.HashMap;

/**
 * Implements a hash table-based map
 * using a random access file structure.
 */
public class HashFile implements HashMap<Object, List<Object>> {
	/*
	 * TODO: For Module 7, implement the stubs.
	 *
	 * Until then, this class is unused.
	 */
	public HashFile(Path path, Entry<Integer, List<String>> descriptor) {
		
	}
	private Entry<Object, List<Object>>[] descript;
	private int size;
	private int capacity;
	Entry<Object, List<Object>> removal= new Entry<>(null,null);
	public HashFile() {
		size = 0;
		capacity = 13;
		descript = (Entry<Object, List<Object>>[]) Array.newInstance(Entry.class,capacity);
	}

	@Override
	public void clear() {
		descript = null;
		descript = (Entry<Object, List<Object>>[]) Array.newInstance(Entry.class,capacity);
		size=0;
	}

	@Override
	public List<Object> put(Object key, List<Object> value) {
		List<Object> val = null;
		
		int index = hashFunction(key);
		int removalIndex = -1;
	
		Entry<Object, List<Object>> entry = descript[index];

		if(entry == removal) {
			removalIndex=index;
		}
		if(entry==null) {
			descript[index]= new Entry<>(key,value);
			size++;
		}
		else if(key == null ? entry.key() == null : key.equals(entry.key())) {
			val = entry.value();
			descript[index]= new Entry<>(key,value);
		}
		else {
			Boolean collisionCheck= false;
			int c = altHashFunction(key);
			
			while(!collisionCheck) {
				index+=c;
				index = index%capacity;

				Entry<Object, List<Object>> nextEntry = descript[index];
			
				if(nextEntry == removal && removalIndex == -1) {
					removalIndex=index;
				}

				if(nextEntry==null) {
					if(removalIndex != -1) {
						descript[removalIndex]= new Entry<>(key,value);
						size++;
						val = null;
						collisionCheck = true;
					}
					else {
						descript[index]= new Entry<>(key,value);
						size++;
						val = null;
						collisionCheck = true;
					}
				}
				else if(key == null ? nextEntry.key() == null : key.equals(nextEntry.key())) {
					if(removalIndex!=-1) {
						val = nextEntry.value();
						descript[index]=removal;
						descript[removalIndex]= new Entry<>(key,value);
						collisionCheck = true;
					}
					else {
						val = nextEntry.value();
						descript[index]= new Entry<>(key,value);
						collisionCheck = true;
					}
				}
			}
		}
		if(loadFactor()>=.75)
				rehash();
				
		return val;
	}

	@Override
	public List<Object> get(Object key) {
		List<Object> value = null;
		int index = hashFunction(key);
		
		Entry<Object, List<Object>> entry = descript[index];
		
		if(entry==null) {
			return value;
		}
		else if(key == null ? entry.key() == null : key.equals(entry.key())) {
			value = entry.value();
			return value;
		}
		else {
			Boolean collisionCheck= false;
			int c = altHashFunction(key);
			while(!collisionCheck) {
				index+=c;
				index = index%capacity;

				Entry<Object, List<Object>> nextEntry = descript[index];

				if(nextEntry==null) {
					collisionCheck= true;
					return value;
				}
				else if(key == null ? nextEntry.key() == null : key.equals(nextEntry.key())) {
					collisionCheck= true;
					value = nextEntry.value();
					return value;
				}
			}
		}
		return value;
	}

	@Override
	public List<Object> remove(Object key) {
	List<Object> removedValue = null;
		
		int index = hashFunction(key);
		
		Entry<Object, List<Object>> entry = descript[index];
		
		if(entry==null) {
			removedValue=null;
		}
		else if(key == null ? entry.key() == null : key.equals(entry.key())) {
			removedValue = entry.value();
			descript[index]= removal;
			size--;
		}
		else {
			Boolean collisionCheck= false;
			int c = altHashFunction(key);
			
			while(!collisionCheck) {
				index+=c;
				index = index%capacity;

				Entry<Object, List<Object>> nextEntry = descript[index];

				if(nextEntry==null) {
					removedValue= null;
					collisionCheck= true;
				}
				else if(key == null ? nextEntry.key() == null : key.equals(nextEntry.key())) {
					removedValue = nextEntry.value();
					descript[index]= removal;
					size--;
					collisionCheck= true;
				}
			}
		}
		if(loadFactor()<=.25 && capacity != 13)
			rehash();
		
		return removedValue;
	}

	@Override
	public boolean contains(Object key) {
		Boolean containCheck = false;
		int index = hashFunction(key);
		
		Entry<Object, List<Object>> entry = descript[index];
		
		if(entry==null) {
			return containCheck;
		}
		else if(key == null ? entry.key() == null : key.equals(entry.key())) {
			containCheck = true;
			return containCheck;
		}
		else {
			Boolean collisionCheck= false;
			int c = altHashFunction(key);
			while(!collisionCheck) {
				index+=c;
				index = index%capacity;

				Entry<Object, List<Object>> nextEntry = descript[index];

				if(nextEntry==null) {
					collisionCheck= true;
					return containCheck;
				}
				else if(key == null ? nextEntry.key() == null : key.equals(nextEntry.key())) {
					collisionCheck= true;
					containCheck = true;
					return containCheck;
				}
			}
		}
		return containCheck;
	}

	@Override
	public int size() {
		return size;
	}

	@Override
	public boolean isEmpty() {
		
		return size == 0;
	}

	@Override
	public String toString() {
		StringBuilder entries = new StringBuilder();
		int entriesCheck=0;
		
		entries.append("{");
		for(int i = 0; i < capacity; i++) {
			if(descript[i]!=null && descript[i]!=removal) {
				if(entriesCheck<size()) {
					entriesCheck++;
					entries.append(descript[i].toString()+", ");
				}
				else {
					entries.append(descript[i].toString());
				}
			}
		}
		entries.append("}");
		
		return entries.toString();
	}

	@Override
	public boolean equals(Object o) {
		if (o instanceof HashMap<?, ?>)
			return this.hashCode() == o.hashCode();
		else return false;
	}

	@Override
	public int hashCode() {
		int sum = 0;
		for (int i = 0; i < capacity; i++) {
			if(descript[i]!=null && descript[i]!=removal)
				sum += descript[i].hashCode();
		}
		return sum;
	}

	@Override
	public double loadFactor() {
		
		return (double) size/capacity;
	}

	@Override
	public Iterator<Entry<Object, List<Object>>> iterator() {
		return new Iterator<>() {
			int index = 0;
			
			@Override
			public boolean hasNext() {
				return index < size();
			}

			@Override
			public Entry<Object, List<Object>> next() {
				return descript[index];
			}
		};
	}

	@Override
	public int hashFunction(Object key) {
		return key.hashCode() % capacity;
	}

	@Override
	public int altHashFunction(Object key) {
		Boolean primeNumCheck = false;
		int num = capacity;
		num*=5;
		num/=8;
		while(!primeNumCheck) {
			for (int i = 2; i <= Math.sqrt(num); ++i) {
				if(num%i==0) {
					primeNumCheck=false;
					num--;
				}
				else {
					primeNumCheck=true;
				}
			}
		}
		
		return num - (key.hashCode() % num);
	}
	
	public int newCapacity(int length) {
		Boolean primeNumCheck = false;
		if(loadFactor()>=.75){
			length/=2;
			while(!primeNumCheck) {
				for (int i = 2; i <= Math.sqrt(length); ++i) {
					if(length%i==0) {
						primeNumCheck=false;
						length--;
					}
					else {
						primeNumCheck=true;
					}
				}
			}
		}
		else if (loadFactor()<=.25){
			length*=2;
			while(!primeNumCheck) {
				for (int i = 2; i <= Math.sqrt(length); ++i) {
					if(length%i==0) {
						primeNumCheck=false;
						length++;
					}
					else {
						primeNumCheck=true;
					}
				}
			}
		}
				
		return length;
	}
	@SuppressWarnings("unchecked")
	public void rehash() {
		
		Entry<Object, List<Object>>[] temp;
		temp = descript;
		int oldLength = capacity;
		descript = null;
		size=0;
		capacity= newCapacity(capacity);
		descript = (Entry<Object, List<Object>>[]) Array.newInstance(Entry.class, capacity);
		
		for(int i = 0; i < oldLength; i++) {
			Entry<Object, List<Object>> entry= temp[i];
			
			if(entry != null && entry != removal) {
				put(entry.key(),entry.value());
			}
		}
	}

	
	
	
}
