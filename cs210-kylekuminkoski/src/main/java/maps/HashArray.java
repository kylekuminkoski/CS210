package maps;

import java.lang.reflect.Array;
import java.util.Iterator;

import types.Entry;
import types.HashMap;

/**
 * Implements a hash table-based map
 * using an array data structure.
 */
public class HashArray<K, V> implements HashMap<K, V> {
	private Entry<K, V>[] list;
	private int size;
	private int capacity;
	Entry<K,V> removal= new Entry<>(null,null);
	
	public HashArray() {
		capacity = 13;
		list = (Entry<K, V>[]) Array.newInstance(Entry.class,capacity);
		size = 0;
		
	}

	@Override
	public void clear() {
		list = null;
		list = (Entry<K, V>[]) Array.newInstance(Entry.class,capacity);
		size=0;
	}

	@Override
	public V put(K key, V value) {
		V val = null;
		
		int index = hashFunction(key);
		int removalIndex = -1;
	
		Entry<K, V> entry = list[index];

		if(entry == removal) {
			removalIndex=index;
		}
		if(entry==null) {
			list[index]= new Entry<>(key,value);
			size++;
		}
		else if(key == null ? entry.key() == null : key.equals(entry.key())) {
			val = entry.value();
			list[index]= new Entry<>(key,value);
		}
		else {
			Boolean collisionCheck= false;
			int c = altHashFunction(key);
			
			while(!collisionCheck) {
				index+=c;
				index = index%capacity;

				Entry<K, V> nextEntry = list[index];
			
				if(nextEntry == removal && removalIndex == -1) {
					removalIndex=index;
				}

				if(nextEntry==null) {
					if(removalIndex != -1) {
						list[removalIndex]= new Entry<>(key,value);
						size++;
						val = null;
						collisionCheck = true;
					}
					else {
						list[index]= new Entry<>(key,value);
						size++;
						val = null;
						collisionCheck = true;
					}
				}
				else if(key == null ? nextEntry.key() == null : key.equals(nextEntry.key())) {
					if(removalIndex!=-1) {
						val = nextEntry.value();
						list[index]=removal;
						list[removalIndex]= new Entry<>(key,value);
						collisionCheck = true;
					}
					else {
						val = nextEntry.value();
						list[index]= new Entry<>(key,value);
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
	public V get(K key) {
		V value = null;
		int index = hashFunction(key);
		
		Entry<K, V> entry = list[index];
		
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

				Entry<K, V> nextEntry = list[index];

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
	public V remove(K key) {
	V removedValue = null;
		
		int index = hashFunction(key);
		
		Entry<K, V> entry = list[index];
		
		if(entry==null) {
			removedValue=null;
		}
		else if(key == null ? entry.key() == null : key.equals(entry.key())) {
			removedValue = entry.value();
			list[index]= removal;
			size--;
		}
		else {
			Boolean collisionCheck= false;
			int c = altHashFunction(key);
			
			while(!collisionCheck) {
				index+=c;
				index = index%capacity;

				Entry<K, V> nextEntry = list[index];

				if(nextEntry==null) {
					removedValue= null;
					collisionCheck= true;
				}
				else if(key == null ? nextEntry.key() == null : key.equals(nextEntry.key())) {
					removedValue = nextEntry.value();
					list[index]= removal;
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
	public boolean contains(K key) {
		Boolean containCheck = false;
		int index = hashFunction(key);
		
		Entry<K, V> entry = list[index];
		
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

				Entry<K, V> nextEntry = list[index];

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
			if(list[i]!=null && list[i]!=removal) {
				if(entriesCheck<size()) {
					entriesCheck++;
					entries.append(list[i].toString()+", ");
				}
				else {
					entries.append(list[i].toString());
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
			if(list[i]!=null && list[i]!=removal)
				sum += list[i].hashCode();
		}
		return sum;
	}

	@Override
	public double loadFactor() {
		
		return (double) size/capacity;
	}

	@Override
	public Iterator<Entry<K, V>> iterator() {
		return new Iterator<>() {
			int index = 0;
			
			@Override
			public boolean hasNext() {
				return index < size();
			}

			@Override
			public Entry<K, V> next() {
				return list[index];
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
		
		Entry<K,V>[] temp;
		temp = list;
		int oldLength = capacity;
		list = null;
		size=0;
		capacity= newCapacity(capacity);
		list = (Entry<K, V>[]) Array.newInstance(Entry.class, capacity);
		
		for(int i = 0; i < oldLength; i++) {
			Entry<K, V> entry= temp[i];
			
			if(entry != null && entry != removal) {
				put(entry.key(),entry.value());
			}
		}
	}
	
	
	
}
