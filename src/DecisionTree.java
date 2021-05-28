import java.io.Serializable;
import java.util.ArrayList;
import java.text.*;
import java.lang.Math;

public class DecisionTree implements Serializable {

	DTNode rootDTNode;
	int minSizeDatalist; //minimum number of datapoints that should be present in the dataset so as to initiate a split

	// Mention the serialVersionUID explicitly in order to avoid getting errors while deserializing.
	public static final long serialVersionUID = 343L;

	public DecisionTree(ArrayList<Datum> datalist , int min) {
		minSizeDatalist = min;
		rootDTNode = (new DTNode()).fillDTNode(datalist);
	}

	class DTNode implements Serializable{
		//Mention the serialVersionUID explicitly in order to avoid getting errors while deserializing.
		public static final long serialVersionUID = 438L;
		boolean leaf;
		int label = -1;      // only defined if node is a leaf
		int attribute; // only defined if node is not a leaf
		double threshold;  // only defined if node is not a leaf

		DTNode left, right; //the left and right child of a particular node. (null if leaf)

		DTNode() {
			leaf = true;
			threshold = Double.MAX_VALUE;
		}


		// this method takes in a datalist (ArrayList of type datum). It returns the calling DTNode object
		// as the root of a decision tree trained using the datapoints present in the datalist variable and minSizeDatalist.
		// Also, KEEP IN MIND that the left and right child of the node correspond to "less than" and "greater than or equal to" threshold
		DTNode fillDTNode(ArrayList<Datum> datalist) {

			int k = minSizeDatalist;//let k be minSizeDatalist
			boolean checker=true; //boolean to keep check for label equality
			ArrayList<Datum> leftChild = new ArrayList<>(); //for STORING the split data in the left
			ArrayList<Datum> rightChild = new ArrayList<>(); //for STORING the other split data

			if(datalist.size()==0){ //if no elements are there in datalist, null is returned
				return null;
			}

			if(k<=datalist.size()){ //if it has k or more data
				for(int i =1;i<datalist.size();i++){ //checking all labels are same or not
					if (!(datalist.get(i - 1).y == datalist.get(i).y)) { //if any label is not same, then checker is false
						checker = false;
						break; //breaking the code so that checker is false and loop breaks if an inequality in label is found
					}
				}
				if(checker){ //if all labels are same,
					DTNode x = new DTNode(); //create a new node
					x.leaf = true; //since its a leaf
					x.right = null;
					x.left = null;
					x.label= datalist.get(datalist.size()-1).y; //setting the label as the last label of the dataset
					return x;
				}
				else{
					//finding the best split (creating best attribute test question)
					double best_average_entropy = Double.POSITIVE_INFINITY; //INFINITY
					int best_attribute = -1;
					double best_threshold = -1;
					double h1; //for the entropy of left split
					double h2; //for the entropy of the right split

					for(int i =0;i<datalist.get(datalist.size()-1).x.length;i++){ //for each attribute in x[]


						for(int n=0;n<datalist.size();n++){ //for each data point in the data list

							ArrayList<Datum> d1 = new ArrayList<>(); //creating an Arraylist to keep left side split
							ArrayList<Datum> d2 = new ArrayList<>(); //creating an Arraylist to keep right side split
							//comparing each datum in the data list
							for (Datum datum : datalist) {
								if (datum.x[i] >= datalist.get(n).x[i]) {
									//add it to the right list
									d2.add(datum);
								}
								if (datum.x[i] < datalist.get(n).x[i]) {
									//add it to the left list
									d1.add(datum);
								}
							}

							h1 = calcEntropy(d1); //calculating entropy of left data list
							h2 = calcEntropy(d2); //calculating entropy of right data list
							double average_entropy = (h1*d1.size())/datalist.size() + (h2*d2.size())/datalist.size();

							if(best_average_entropy>average_entropy) { //pseudocode in the question
								best_average_entropy = average_entropy;
								best_attribute = i;// i is the index of the x[], so i is that attribute
								best_threshold = datalist.get(n).x[i];

								leftChild = d1; //so i can use leftChild later
								rightChild = d2; //so i can use rightChild later
							}
						}
					}

					//if minimum average entropy equals entropy of the data list (edge case)
					if(best_average_entropy==calcEntropy(datalist)) {
						DTNode mini = new DTNode(); //created new leaf node
						mini.leaf = true; //since it's a leaf
						mini.left = null;
						mini.right = null;
						mini.label = findMajority(datalist);// label of the node is the majority of labels
						return mini;
					}

					DTNode nodeX = new DTNode(); //creating a new node
					nodeX.leaf = false; //not a leaf

					nodeX.threshold = best_threshold; //setting threshold
					nodeX.attribute = best_attribute; //setting attribute
					nodeX.left = fillDTNode(leftChild); //recursive call on leftChild
					nodeX.right = fillDTNode(rightChild); //recursive call on rightChild
					return nodeX;
				}
			}
			//if datalist doesn't have at least k data items
			DTNode leafy = new DTNode(); //create new leaf node
			leafy.leaf = true; //since it's a leaf
			leafy.left = null;
			leafy.right = null;
			leafy.label= findMajority(datalist);// label of the node is the majority of labels
			return leafy;
		}


		// This is a helper method. Given a datalist, this method returns the label that has the most
		// occurrences. In case of a tie it returns the label with the smallest value (numerically) involved in the tie.
		int findMajority(ArrayList<Datum> datalist) {

			int [] votes = new int[2];

			//loop through the data and count the occurrences of datapoints of each label
			for (Datum data : datalist)
			{
				votes[data.y]+=1;
			}

			if (votes[0] >= votes[1])
				return 0;
			else
				return 1;
		}




		// This method takes in a datapoint (excluding the label) in the form of an array of type double (Datum.x) and
		// returns its corresponding label, as determined by the decision tree
		int classifyAtNode(double[] xQuery) {
			//if leaf
			if(this.leaf) return this.label; //returning the label
			//or else test the data item
			else{
				if(xQuery[attribute]>=threshold){  //if attribute is greater than or equal to threshold
					return right.classifyAtNode(xQuery); //go to RIGHT child and run method recursively
				}
				return left.classifyAtNode(xQuery); //or else attribute is smaller than threshold, go to LEFT child
			}                                       //then run method recursively again!
		}

		//given another DTNode object, this method checks if the tree rooted at the calling DTNode is equal to the tree rooted
		//at DTNode object passed as the parameter
		public boolean equals(Object dt2)
		{
			if(dt2==null ||!(dt2 instanceof  DTNode)){ //if object is null or not of type DTNode, then return false
				return false;
			}
			DTNode x = (DTNode) dt2; //setting x as dt2

			//when internal nodes, they are not leaves
			//their left and right has to be equal to be true
			//also attributes and threshold must match
			//left and right matched recursively
			if(!(this.leaf && x.leaf)){
				return ((this.attribute==x.attribute && this.threshold==x.threshold)&&(this.left.equals(x.left)) &&(this.right.equals(x.right)));
			}
			//now if both are leaves,
			//labels must match if true
			//left and right are null as its a leaf
			 if(this.leaf && x.leaf){
				return (this.label==x.label);
			}
			return false; //else false
		}
	}


	//Given a dataset, this returns the entropy of the dataset
	double calcEntropy(ArrayList<Datum> datalist) {
		double entropy = 0;
		double px = 0;
		float [] counter= new float[2];
		if (datalist.size()==0)
			return 0;
		double num0 = 0.00000001,num1 = 0.000000001;

		//calculates the number of points belonging to each of the labels
		for (Datum d : datalist)
		{
			counter[d.y]+=1;
		}
		//calculates the entropy using the formula specified in the document
		for (int i = 0 ; i< counter.length ; i++)
		{
			if (counter[i]>0)
			{
				px = counter[i]/datalist.size();
				entropy -= (px*Math.log(px)/Math.log(2));
			}
		}

		return entropy;
	}


	// given a datapoint (without the label) calls the DTNode.classifyAtNode() on the rootnode of the calling DecisionTree object
	int classify(double[] xQuery ) {
		return this.rootDTNode.classifyAtNode( xQuery );
	}

	// Checks the performance of a DecisionTree on a dataset
	// This method is provided in case you would like to compare your
	// results with the reference values provided in the PDF in the Data
	// section of the PDF
	String checkPerformance( ArrayList<Datum> datalist) {
		DecimalFormat df = new DecimalFormat("0.000");
		float total = datalist.size();
		float count = 0;

		for (int s = 0 ; s < datalist.size() ; s++) {
			double[] x = datalist.get(s).x;
			int result = datalist.get(s).y;
			if (classify(x) != result) {
				count = count + 1;
			}
		}

		return df.format((count/total));
	}


	//Given two DecisionTree objects, this method checks if both the trees are equal by
	//calling onto the DTNode.equals() method
	public static boolean equals(DecisionTree dt1,  DecisionTree dt2)
	{
		boolean flag = true;
		flag = dt1.rootDTNode.equals(dt2.rootDTNode);
		return flag;
	}

}
