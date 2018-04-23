package words;

public class Quicksort {
    
    public static <T extends Comparable<T>> void quickSort(T[] a) {
        qSort(a, 0, a.length-1);
    }

    private static <T extends Comparable<T>> void qSort(T[] a, int low, int high) {
        if(low < high) {
            int pivotIndex = partition(a, low, high);
            qSort(a, low, pivotIndex-1);
            qSort(a, pivotIndex+1, high);
        }
    }
    
    private static <T extends Comparable<T>> int partition(T[] a, int low, int high) {
        int pivotIndex = choosePivot(a, low, high);
        T pivot = a[pivotIndex];
        // Move the pivot to a[low] to simplify subsequent code.
        swap(a, pivotIndex, low);
        int i = low+1;
        int j = high;
        while(i <= j) {
            // low < i && i <= j && j <= high
            // && (0 <= k < i --> pivot.compareTo(a[k]) >= 0)
            // && (j <= k <= high --> pivot.compareTo(a[k]) < 0)
            
            // Move i forward if a[i] less than or equal to the pivot element.
            if(a[i].compareTo(pivot) <= 0)
                i++;
            // Move j backward if a[j] greater than the pivot element.
            else if(a[j].compareTo(pivot) > 0)
                j--;
            // Exchange large element at i with small element at j.
            else
                swap(a, i++, j);
            
            // low < i && i-1 <= j && j <= high
            // && (0 <= k < i --> pivot.compareTo(a[k]) >= 0)
            // && (j <= k <= high --> pivot.compareTo(a[k]) < 0)
       }
       // low < i-1 == j <= high
       // && (0 <= k < i --> pivot.compareTo(a[k]) >= 0)
       // && (j <= k <= high --> pivot.compareTo(a[k]) < 0)
       swap(a, low, --i);
       return i;
    }
    
    private static <T extends Comparable<T>> int choosePivot(T[] a, int low, int high) {
        // Compute median of a[low], a[high], a[(low+high)/2].
        int mid = (low+high)/2;
        if(a[low].compareTo(a[mid]) > 0)
            swap(a, low, mid);
        if(a[mid].compareTo(a[high]) > 0)
            swap(a, mid, high);
        if(a[low].compareTo(a[mid]) > 0)
            swap(a, low, mid);
        return mid;
    }
    
   private static <T extends Comparable<T>> void swap(T[] a, int i, int j) {
	T t = a[i];
	a[i] = a[j];
	a[j] = t;
    }
   
   private static <T extends Comparable<T>> void
         show(String msg, T[] a, int low, int pivot, int high) {
       System.out.print(msg);
       for(int i = 0; i < a.length; i++) {
	   if(i == low)
	       System.out.print("[ ");
	   if(i == pivot)
	       System.out.print("( ");
	   if(i < a.length)
	     System.out.print(a[i] + " ");
	   if(i == pivot)
	       System.out.print(") ");
	   if(i == high)
	       System.out.print("] ");
       }
       System.out.println();
   }
    
    private static <T extends Comparable<T>> void test(T[] a) {
	quickSort(a);
	for(int i = 0; i < a.length-1; i++) {
	    if(a[i].compareTo(a[i+1]) > 0) {
		show("***ERROR*** ", a, 0, -1, a.length-1);
		return;
	    }
	}
    }
    
    public static void main(String[] args) {
	test(new String[0]);
	test(new String[] { "dog" });
	test(new String[] { "dog", "cat" });
	test(new String[] { "cat", "dog" });
	test(new String[] { "cat", "dog", "bird" });
	test(new String[] { "cat", "dog", "dog" });
	test(new String[] { "cat", "cat", "dog" });
	test(new String[] { "cat", "dog", "cat" });
	test(new String[] { "dog", "cat", "bird", "monkey", "cheetah", "giraffe", "elephant",
			    "fox", "weasel", "narwhal", "ibex" });
    }

}
