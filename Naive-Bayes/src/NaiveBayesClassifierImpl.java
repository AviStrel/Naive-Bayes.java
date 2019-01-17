import java.util.HashMap;
import java.util.Map;
import java.lang.Math;

/**
 * Your implementation of a naive bayes classifier. Please implement all four methods.
 */

public class NaiveBayesClassifierImpl implements NaiveBayesClassifier {
	private Instance[] m_trainingData;
	private int m_v;
	private double m_delta;
	public int m_sports_count, m_business_count;
	public int m_sports_word_count, m_business_word_count;
	private HashMap<String,Integer> m_map[] = new HashMap[2];

  /**
   * Trains the classifier with the provided training data and vocabulary size
   */
  @Override
  public void train(Instance[] trainingData, int v) {
    // TODO : Implement
    // For all the words in the documents, count the number of occurrences. Save in HashMap
    // e.g.
    // m_map[0].get("catch") should return the number of "catch" es, in the documents labeled sports
    // Hint: m_map[0].get("asdasd") would return null, when the word has not appeared before.
    // Use m_map[0].put(word,1) to put the first count in.
    // Use m_map[0].replace(word, count+1) to update the value
  	  m_trainingData = trainingData;
  	  m_v = v;
  	  m_map[0] = new HashMap<>();
  	  m_map[1] = new HashMap<>();

  	  int index = 0; // 0 for sports , 1 for business
      Integer count = 0;

      documents_per_label_count(trainingData);
      words_per_label_count(trainingData);

  	  for(Instance train : trainingData){
          //************* Get the index of the train's label *************
          if(train.label == Label.SPORTS) index = 0;//0 for sports
          else index = 1;//1 for business

          for(String word : train.words){
             count = m_map[index].get(word);
             if(count == null) m_map[index].put(word,1); // First appearance of word
             else m_map[index].replace(word,count+1); //Update the number that word appeared in the training set
          }

      }
  }

  /*
   * Counts the number of documents for each label
   */
  public void documents_per_label_count(Instance[] trainingData){
    // TODO : Implement
    m_sports_count = 0;
    m_business_count = 0;
    for(Instance ins : trainingData){
        if(ins.label == Label.SPORTS) m_sports_count++;
        if(ins.label == Label.BUSINESS) m_business_count++;
    }

  }

  /*
   * Prints the number of documents for each label
   */
  public void print_documents_per_label_count(){
  	  System.out.println("SPORTS=" + m_sports_count);
  	  System.out.println("BUSINESS=" + m_business_count);
  }


  /*
   * Counts the total number of words for each label
   */
  public void words_per_label_count(Instance[] trainingData){
    // TODO : Implement
    m_sports_word_count = 0;
    m_business_word_count = 0;
    for(Instance ins : trainingData){
        if(ins.label == Label.SPORTS) m_sports_word_count +=ins.words.length;
        if(ins.label == Label.BUSINESS) m_business_word_count +=ins.words.length;
    }

  }

  /*
   * Prints out the number of words for each label
   */
  public void print_words_per_label_count(){
  	  System.out.println("SPORTS=" + m_sports_word_count);
  	  System.out.println("BUSINESS=" + m_business_word_count);
  }

  /**
   * Returns the prior probability of the label parameter, i.e. P(SPORTS) or P(BUSINESS)
   */
  @Override
  public double p_l(Label label) {
    // TODO : Implement
    // Calculate the probability for the label. No smoothing here.
    // Just the number of label counts divided by the number of documents.
    double ret = 0;

    int total_number_of_documents = m_business_count + m_sports_count;
    int number_of_documents_per_given_label = 0;
    if(label == Label.SPORTS) number_of_documents_per_given_label = m_sports_count;
    if(label == Label.BUSINESS) number_of_documents_per_given_label = m_business_count;
    ret = (double)number_of_documents_per_given_label/(double) total_number_of_documents;
    return ret;
  }

  /**
   * Returns the smoothed conditional probability of the word given the label, i.e. P(word|SPORTS) or
   * P(word|BUSINESS)
   */
  @Override
  public double p_w_given_l(String word, Label label) {
    // TODO : Implement
    // Calculate the probability with Laplace smoothing for word in class(label)
    double ret = 0;
    m_delta = 0.00001;
    //************* Initialize *************
    HashMap<String,Integer> m=null;
    Integer word_number_of_appearance;
    double number_of_words_per_given_label=0;
    if(label == Label.SPORTS) {
        m = m_map[0];
        number_of_words_per_given_label = m_sports_word_count;
    }
    if(label == Label.BUSINESS) {
        m = m_map[1];
        number_of_words_per_given_label = m_business_word_count;
    }
    if(m == null) return ret;

    //************* Calculating by the given formula at page 3 at the pdf *************
    word_number_of_appearance = m.get(word);
    if (word_number_of_appearance == null) word_number_of_appearance=0;

    double numerator = word_number_of_appearance + m_delta;
    double denominator = (double) m_v * m_delta + number_of_words_per_given_label;

    ret = numerator / denominator;
    return ret;
  }

  /**
   * Classifies an array of words as either SPORTS or BUSINESS.
   */
  @Override
  public ClassifyResult classify(String[] words) {
    // TODO : Implement
    // Sum up the log probabilities for each word in the input data, and the probability of the label
    // Set the label to the class with larger log probability
    ClassifyResult ret = new ClassifyResult();
    ret.label = Label.SPORTS;
    ret.log_prob_sports = 0;
    ret.log_prob_business = 0;

    //************* Calculating by the given formula at page 4 at the pdf *************
    ret.log_prob_sports = Math.log(p_l(Label.SPORTS));
    ret.log_prob_business = Math.log(p_l(Label.BUSINESS));

    for (String word : words){
        ret.log_prob_sports += Math.log(p_w_given_l(word, Label.SPORTS));
        ret.log_prob_business += Math.log(p_w_given_l(word,Label.BUSINESS));
    }

    if(ret.log_prob_business>ret.log_prob_sports) ret.label = Label.BUSINESS;
    return ret; 
  }
  
  /*
   * Constructs the confusion matrix
   */
  @Override
  public ConfusionMatrix calculate_confusion_matrix(Instance[] testData){
    // TODO : Implement
    // Count the true positives, true negatives, false positives, false negatives
    int TP, FP, FN, TN;
    TP = 0;
    FP = 0;
    FN = 0;
    TN = 0;

    Label temp;
    for(Instance test : testData){
        temp = classify(test.words).label;
        if(temp == Label.SPORTS) {
            if (test.label == Label.SPORTS) TP++;
            else FP++;
        }
        if(temp == Label.BUSINESS){
            if(test.label ==Label.BUSINESS) TN++;
            else FN++;
        }
    }
    return new ConfusionMatrix(TP,FP,FN,TN);
  }
  
}

