/* Copyright 2019 The TensorFlow Authors. All Rights Reserved.

Licensed under the Apache License, Version 2.0 (the "License");
you may not use this file except in compliance with the License.
You may obtain a copy of the License at

    http://www.apache.org/licenses/LICENSE-2.0

Unless required by applicable law or agreed to in writing, software
distributed under the License is distributed on an "AS IS" BASIS,
WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
See the License for the specific language governing permissions and
limitations under the License.
==============================================================================*/

package com.p4f.esp32camai.tflite;

import android.content.res.AssetFileDescriptor;
import android.content.res.AssetManager;
import android.graphics.Bitmap;
import android.graphics.RectF;
import android.os.SystemClock;
import android.util.Log;

import com.p4f.esp32camai.MyConstants;

import org.tensorflow.lite.Interpreter;
import org.tensorflow.lite.gpu.GpuDelegate;

import java.io.FileInputStream;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.MappedByteBuffer;
import java.nio.channels.FileChannel;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Vector;

public class TFLiteObjectDetectionSSDAPIModel extends Classifier {

  final static String TAG = "TFLiteObjectDetectionSSDAPIModel";

  /** An immutable result returned by a Classifier describing what was recognized. */
  public class Recognition {
    /**
     * A unique identifier for what has been recognized. Specific to the class, not the instance of
     * the object.
     */
    private final String id;

    /** Display name for the recognition. */
    private final String title;

    /**
     * A sortable score for how good the recognition is relative to others. Higher should be better.
     */
    private final Float confidence;

    /** Optional location within the source image for the location of the recognized object. */
    private RectF location;

    /** Time execution for detect the recognition by running the tflite*/
    private long timeExecution;

    public Recognition(
            final String id, final String title, final Float confidence, final RectF location, final long timeEx) {
      this.id = id;
      this.title = title;
      this.confidence = confidence;
      this.location = location;
      this.timeExecution = timeEx;
    }

    public String getId() {
      return id;
    }

    public String getTitle() {
      return title;
    }

    public Float getConfidence() {
      return confidence;
    }

    public RectF getLocation() {
      return new RectF(location);
    }

    public void setLocation(RectF location) {
      this.location = location;
    }

    public long getTimeExecution(){
      return this.timeExecution;
    }

    @Override
    public String toString() {
      String resultString = "";
      if (id != null) {
        resultString += "[" + id + "] ";
      }

      if (title != null) {
        resultString += title + " ";
      }

      if (confidence != null) {
        resultString += String.format("(%.1f%%) ", confidence * 100.0f);
      }

      if (location != null) {
        resultString += location + " ";
      }

      resultString += "in " + timeExecution + " ms";

      return resultString.trim();
    }
  }

  public Vector<String> getLabels(){
    return labels;
  }

  public static float sigmoid(float x) {
    return (float)(1/( 1 + Math.pow(Math.E,(-1*x))));
  }

  /** Memory-map the model file in Assets. */
  private static MappedByteBuffer loadModelFile(AssetManager assets, String modelFilename)
          throws IOException {
    AssetFileDescriptor fileDescriptor = assets.openFd(modelFilename);
    FileInputStream inputStream = new FileInputStream(fileDescriptor.getFileDescriptor());
    FileChannel fileChannel = inputStream.getChannel();
    long startOffset = fileDescriptor.getStartOffset();
    long declaredLength = fileDescriptor.getDeclaredLength();
    return fileChannel.map(FileChannel.MapMode.READ_ONLY, startOffset, declaredLength);
  }

  /** Optional GPU delegate for accleration. */
  private GpuDelegate gpuDelegateSSD = null;

  // Number of threads in the java app
  private static final int NUM_THREADS = 4;

  // Pre-allocated buffers.
  private Vector<String> labels = new Vector<String>();

  /** Options for configuring the Interpreter. */
  private final Interpreter.Options tfliteOptionsSSD = new Interpreter.Options();

  //>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>for SSD mobilenetv2<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<
  private static final float IMAGE_MEAN_SSD = 128.0f;
  private static final float IMAGE_STD_SSD = 128.0f;
  // Only return this many results.
  private static final int NUM_DETECTIONS = 10;
  private int[] intValuesSSD;
  private int   inputSizeSSD;
  private float[][][] ouputSSDBoxes;
  private float[][]   ouputSSDClasses;
  private float[][]   ouputSSDScores;

  private ByteBuffer imgDataSSD;

  private Interpreter tfLiteSSD;

  private float scoreThreshold;

  MyConstants.MODEL_TYPE modelType = MyConstants.MODEL_TYPE.FLOAT32;

  private List<Recognition> resultsSSD = new ArrayList<>();
  private List<Recognition> resultsSSDPre = new ArrayList<>();

  final String[] labelsSSD = {
          "Tortoise",
          "Container",
          "Magpie",
          "Sea turtle",
          "Football",
          "Ambulance",
          "Ladder",
          "Toothbrush",
          "Syringe",
          "Sink",
          "Toy",
          "Organ",
          "Cassette deck",
          "Apple",
          "Human eye",
          "Cosmetics",
          "Paddle",
          "Snowman",
          "Beer",
          "Chopsticks",
          "Human beard",
          "Bird",
          "Parking meter",
          "Traffic light",
          "Croissant",
          "Cucumber",
          "Radish",
          "Towel",
          "Doll",
          "Skull",
          "Washing machine",
          "Glove",
          "Tick",
          "Belt",
          "Sunglasses",
          "Banjo",
          "Cart",
          "Ball",
          "Backpack",
          "Bicycle",
          "Home appliance",
          "Centipede",
          "Boat",
          "Surfboard",
          "Boot",
          "Headphones",
          "Hot dog",
          "Shorts",
          "Fast food",
          "Bus",
          "Boy",
          "Screwdriver",
          "Bicycle wheel",
          "Barge",
          "Laptop",
          "Miniskirt",
          "Drill",
          "Dress",
          "Bear",
          "Waffle",
          "Pancake",
          "Brown bear",
          "Woodpecker",
          "Blue jay",
          "Pretzel",
          "Bagel",
          "Tower",
          "Teapot",
          "Person",
          "Bow and arrow",
          "Swimwear",
          "Beehive",
          "Brassiere",
          "Bee",
          "Bat",
          "Starfish",
          "Popcorn",
          "Burrito",
          "Chainsaw",
          "Balloon",
          "Wrench",
          "Tent",
          "Vehicle registration plate",
          "Lantern",
          "Toaster",
          "Flashlight",
          "Billboard",
          "Tiara",
          "Limousine",
          "Necklace",
          "Carnivore",
          "Scissors",
          "Stairs",
          "Computer keyboard",
          "Printer",
          "Traffic sign",
          "Chair",
          "Shirt",
          "Poster",
          "Cheese",
          "Sock",
          "Fire hydrant",
          "Land vehicle",
          "Earrings",
          "Tie",
          "Watercraft",
          "Cabinetry",
          "Suitcase",
          "Muffin",
          "Bidet",
          "Snack",
          "Snowmobile",
          "Clock",
          "Medical equipment",
          "Cattle",
          "Cello",
          "Jet ski",
          "Camel",
          "Coat",
          "Suit",
          "Desk",
          "Cat",
          "Bronze sculpture",
          "Juice",
          "Gondola",
          "Beetle",
          "Cannon",
          "Computer mouse",
          "Cookie",
          "Office building",
          "Fountain",
          "Coin",
          "Calculator",
          "Cocktail",
          "Computer monitor",
          "Box",
          "Stapler",
          "Christmas tree",
          "Cowboy hat",
          "Hiking equipment",
          "Studio couch",
          "Drum",
          "Dessert",
          "Wine rack",
          "Drink",
          "Zucchini",
          "Ladle",
          "Human mouth",
          "Dairy",
          "Dice",
          "Oven",
          "Dinosaur",
          "Ratchet",
          "Couch",
          "Cricket ball",
          "Winter melon",
          "Spatula",
          "Whiteboard",
          "Pencil sharpener",
          "Door",
          "Hat",
          "Shower",
          "Eraser",
          "Fedora",
          "Guacamole",
          "Dagger",
          "Scarf",
          "Dolphin",
          "Sombrero",
          "Tin can",
          "Mug",
          "Tap",
          "Harbor seal",
          "Stretcher",
          "Can opener",
          "Goggles",
          "Human body",
          "Roller skates",
          "Coffee cup",
          "Cutting board",
          "Blender",
          "Plumbing fixture",
          "Stop sign",
          "Office supplies",
          "Volleyball",
          "Vase",
          "Slow cooker",
          "Wardrobe",
          "Coffee",
          "Whisk",
          "Paper towel",
          "Personal care",
          "Food",
          "Sun hat",
          "Tree house",
          "Flying disc",
          "Skirt",
          "Gas stove",
          "Salt and pepper shakers",
          "Mechanical fan",
          "Face powder",
          "Fax",
          "Fruit",
          "French fries",
          "Nightstand",
          "Barrel",
          "Kite",
          "Tart",
          "Treadmill",
          "Fox",
          "Flag",
          "Horn",
          "Window blind",
          "Human foot",
          "Golf cart",
          "Jacket",
          "Egg",
          "Street light",
          "Guitar",
          "Pillow",
          "Human leg",
          "Isopod",
          "Grape",
          "Human ear",
          "Power plugs and sockets",
          "Panda",
          "Giraffe",
          "Woman",
          "Door handle",
          "Rhinoceros",
          "Bathtub",
          "Goldfish",
          "Houseplant",
          "Goat",
          "Baseball bat",
          "Baseball glove",
          "Mixing bowl",
          "Marine invertebrates",
          "Kitchen utensil",
          "Light switch",
          "House",
          "Horse",
          "Stationary bicycle",
          "Hammer",
          "Ceiling fan",
          "Sofa bed",
          "Adhesive tape",
          "Harp",
          "Sandal",
          "Bicycle helmet",
          "Saucer",
          "Harpsichord",
          "Human hair",
          "Heater",
          "Harmonica",
          "Hamster",
          "Curtain",
          "Bed",
          "Kettle",
          "Fireplace",
          "Scale",
          "Drinking straw",
          "Insect",
          "Hair dryer",
          "Kitchenware",
          "Indoor rower",
          "Invertebrate",
          "Food processor",
          "Bookcase",
          "Refrigerator",
          "Wood-burning stove",
          "Punching bag",
          "Common fig",
          "Cocktail shaker",
          "Jaguar",
          "Golf ball",
          "Fashion accessory",
          "Alarm clock",
          "Filing cabinet",
          "Artichoke",
          "Table",
          "Tableware",
          "Kangaroo",
          "Koala",
          "Knife",
          "Bottle",
          "Bottle opener",
          "Lynx",
          "Lavender",
          "Lighthouse",
          "Dumbbell",
          "Human head",
          "Bowl",
          "Humidifier",
          "Porch",
          "Lizard",
          "Billiard table",
          "Mammal",
          "Mouse",
          "Motorcycle",
          "Musical instrument",
          "Swim cap",
          "Frying pan",
          "Snowplow",
          "Bathroom cabinet",
          "Missile",
          "Bust",
          "Man",
          "Waffle iron",
          "Milk",
          "Ring binder",
          "Plate",
          "Mobile phone",
          "Baked goods",
          "Mushroom",
          "Crutch",
          "Pitcher",
          "Mirror",
          "Lifejacket",
          "Table tennis racket",
          "Pencil case",
          "Musical keyboard",
          "Scoreboard",
          "Briefcase",
          "Kitchen knife",
          "Nail",
          "Tennis ball",
          "Plastic bag",
          "Oboe",
          "Chest of drawers",
          "Ostrich",
          "Piano",
          "Girl",
          "Plant",
          "Potato",
          "Hair spray",
          "Sports equipment",
          "Pasta",
          "Penguin",
          "Pumpkin",
          "Pear",
          "Infant bed",
          "Polar bear",
          "Mixer",
          "Cupboard",
          "Jacuzzi",
          "Pizza",
          "Digital clock",
          "Pig",
          "Reptile",
          "Rifle",
          "Lipstick",
          "Skateboard",
          "Raven",
          "High heels",
          "Red panda",
          "Rose",
          "Rabbit",
          "Sculpture",
          "Saxophone",
          "Shotgun",
          "Seafood",
          "Submarine sandwich",
          "Snowboard",
          "Sword",
          "Picture frame",
          "Sushi",
          "Loveseat",
          "Ski",
          "Squirrel",
          "Tripod",
          "Stethoscope",
          "Submarine",
          "Scorpion",
          "Segway",
          "Training bench",
          "Snake",
          "Coffee table",
          "Skyscraper",
          "Sheep",
          "Television",
          "Trombone",
          "Tea",
          "Tank",
          "Taco",
          "Telephone",
          "Torch",
          "Tiger",
          "Strawberry",
          "Trumpet",
          "Tree",
          "Tomato",
          "Train",
          "Tool",
          "Picnic basket",
          "Cooking spray",
          "Trousers",
          "Bowling equipment",
          "Football helmet",
          "Truck",
          "Measuring cup",
          "Coffeemaker",
          "Violin",
          "Vehicle",
          "Handbag",
          "Paper cutter",
          "Wine",
          "Weapon",
          "Wheel",
          "Worm",
          "Wok",
          "Whale",
          "Zebra",
          "Auto part",
          "Jug",
          "Pizza cutter",
          "Cream",
          "Monkey",
          "Lion",
          "Bread",
          "Platter",
          "Chicken",
          "Eagle",
          "Helicopter",
          "Owl",
          "Duck",
          "Turtle",
          "Hippopotamus",
          "Crocodile",
          "Toilet",
          "Toilet paper",
          "Squid",
          "Clothing",
          "Footwear",
          "Lemon",
          "Spider",
          "Deer",
          "Frog",
          "Banana",
          "Rocket",
          "Wine glass",
          "Countertop",
          "Tablet computer",
          "Waste container",
          "Swimming pool",
          "Dog",
          "Book",
          "Elephant",
          "Shark",
          "Candle",
          "Leopard",
          "Axe",
          "Hand dryer",
          "Soap dispenser",
          "Porcupine",
          "Flower",
          "Canary",
          "Cheetah",
          "Palm tree",
          "Hamburger",
          "Maple",
          "Building",
          "Fish",
          "Lobster",
          "Asparagus",
          "Furniture",
          "Hedgehog",
          "Airplane",
          "Spoon",
          "Otter",
          "Bull",
          "Oyster",
          "Horizontal bar",
          "Convenience store",
          "Bomb",
          "Bench",
          "Ice cream",
          "Caterpillar",
          "Butterfly",
          "Parachute",
          "Orange",
          "Antelope",
          "Beaker",
          "Moths and butterflies",
          "Window",
          "Closet",
          "Castle",
          "Jellyfish",
          "Goose",
          "Mule",
          "Swan",
          "Peach",
          "Coconut",
          "Seat belt",
          "Raccoon",
          "Chisel",
          "Fork",
          "Lamp",
          "Camera",
          "Squash",
          "Racket",
          "Human face",
          "Human arm",
          "Vegetable",
          "Diaper",
          "Unicycle",
          "Falcon",
          "Chime",
          "Snail",
          "Shellfish",
          "Cabbage",
          "Carrot",
          "Mango",
          "Jeans",
          "Flowerpot",
          "Pineapple",
          "Drawer",
          "Stool",
          "Envelope",
          "Cake",
          "Dragonfly",
          "Sunflower",
          "Microwave oven",
          "Honeycomb",
          "Marine mammal",
          "Sea lion",
          "Ladybug",
          "Shelf",
          "Watch",
          "Candy",
          "Salad",
          "Parrot",
          "Handgun",
          "Sparrow",
          "Van",
          "Grinder",
          "Spice rack",
          "Light bulb",
          "Corded phone",
          "Sports uniform",
          "Tennis racket",
          "Wall clock",
          "Serving tray",
          "Kitchen & dining room table",
          "Dog bed",
          "Cake stand",
          "Cat furniture",
          "Bathroom accessory",
          "Facial tissue holder",
          "Pressure cooker",
          "Kitchen appliance",
          "Tire",
          "Ruler",
          "Luggage and bags",
          "Microphone",
          "Broccoli",
          "Umbrella",
          "Pastry",
          "Grapefruit",
          "Band-aid",
          "Animal",
          "Bell pepper",
          "Turkey",
          "Lily",
          "Pomegranate",
          "Doughnut",
          "Glasses",
          "Human nose",
          "Pen",
          "Ant",
          "Car",
          "Aircraft",
          "Human hand",
          "Skunk",
          "Teddy bear",
          "Watermelon",
          "Cantaloupe",
          "Dishwasher",
          "Flute",
          "Balance beam",
          "Sandwich",
          "Shrimp",
          "Sewing machine",
          "Binoculars",
          "Rays and skates",
          "Ipod",
          "Accordion",
          "Willow",
          "Crab",
          "Crown",
          "Seahorse",
          "Perfume",
          "Alpaca",
          "Taxi",
          "Canoe",
          "Remote control",
          "Wheelchair",
          "Rugby ball",
          "Armadillo",
          "Maracas",
          "Helmet",



  };

  private TFLiteObjectDetectionSSDAPIModel() {}

  /**
   * Initializes a native TensorFlow session for classifying images.
   *
   * @param assetManager The asset manager to be used to load assets.
   * @param modelFilename The filepath of the model GraphDef protocol buffer.
   * @param labelFilename The filepath of label file for classes.
   * @param inputSize The size of image input for YOLOv3
   */
  public static Classifier create(
      final AssetManager assetManager,
      final String modelFilename,
      final String labelFilename,
      final int inputSize,
      final Device device,
      final MyConstants.MODEL_TYPE modelType,
      final float scoreThreshold,
      final int queueSize,
      final int width,
      final int height)
      throws IOException {
    final TFLiteObjectDetectionSSDAPIModel d = new TFLiteObjectDetectionSSDAPIModel();

//    InputStream labelsInput = null;
//    String actualFilename = labelFilename.split("file:///android_asset/")[1];
//    labelsInput = assetManager.open(actualFilename);
//    BufferedReader br = null;
//    br = new BufferedReader(new InputStreamReader(labelsInput));
//    String line;
//    while ((line = br.readLine()) != null) {
//      Log.d(TAG,line);
//      d.labels.add(line);
//    }
//    br.close();

    d.modelType = modelType;

    final String strModelSSD = modelFilename;

    switch (device) {
      case NNAPI:
        d.tfliteOptionsSSD.setUseNNAPI(true);
        break;
      case GPU:
        d.gpuDelegateSSD = new GpuDelegate();
        d.tfliteOptionsSSD.addDelegate(d.gpuDelegateSSD);
        Log.d("GPU delegated for SSD mobilenet","");
        break;
      case CPU:
        break;
    }

    d.tfliteOptionsSSD.setNumThreads(NUM_THREADS);

    try {
      d.tfLiteSSD = new Interpreter(loadModelFile(assetManager, strModelSSD), d.tfliteOptionsSSD);
    } catch (Exception e) {
      throw new RuntimeException(e);
    }

    int numBytesPerChannelSSD = 4;
    if (modelType == MyConstants.MODEL_TYPE.UINT8) {
      // Pre-allocate buffers.
      numBytesPerChannelSSD = 1; // Floating point
    }

    // >>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>SSD-Mobiblenetv2<<<<<<<<<<<<<<<<<<<<<
    d.inputSizeSSD    = inputSize; //borrow from yolo for a while
    d.imgDataSSD      = ByteBuffer.allocateDirect(1 * d.inputSizeSSD * d.inputSizeSSD * 3 * numBytesPerChannelSSD);
    d.imgDataSSD.order(ByteOrder.nativeOrder());
    d.intValuesSSD    = new int[d.inputSizeSSD * d.inputSizeSSD];
    d.ouputSSDBoxes   = new float[1][NUM_DETECTIONS][4];
    d.ouputSSDClasses = new float[1][NUM_DETECTIONS];
    d.ouputSSDScores  = new float[1][NUM_DETECTIONS];
    d.scoreThreshold = scoreThreshold;


    d.bmpQueue = new LinkedList<Bitmap>()
    {
      @Override
      public boolean add(Bitmap eldest)
      {
        if (this.size() >= queueSize){
          this.remove();
          return super.add(eldest);
        }else{
          return super.add(eldest);
        }
      }
    };

    d.threadDectection = new Thread(d);

    d.width = width;
    d.height = height;

    return d;
  }

  /**
   * Get object results from network
   *
   * @param results store the result.
   *
   * @return true(result is ready for reading)/false(result not ready for reading)
   */
  public void getResult(List<Recognition> results){
    synchronized (lockResult){
      results.clear();
      for(Recognition ret : resultsSSDPre){
        results.add(ret);
      }
    }
  }

  @Override
  protected void processing(Bitmap bmp){
    resultsSSD = recognizeImageSsdMovileNetv2(bmp);
    synchronized (lockResult){
      resultsSSDPre.clear();
      for(Recognition ret : resultsSSD){
        resultsSSDPre.add(ret);
      }
    }
  }

  @Deprecated @SuppressWarnings ( "FloatingPointEquality" )
  public static short toHalfFloat(final float v)
  {
    if(Float.isNaN(v)) throw new UnsupportedOperationException("NaN to half conversion not supported!");
    if(v == Float.POSITIVE_INFINITY) return(short)0x7c00;
    if(v == Float.NEGATIVE_INFINITY) return(short)0xfc00;
    if(v == 0.0f) return(short)0x0000;
    if(v == -0.0f) return(short)0x8000;
    if(v > 65504.0f) return 0x7bff;  // max value supported by half float
    if(v < -65504.0f) return(short)( 0x7bff | 0x8000 );
    if(v > 0.0f && v < 5.96046E-8f) return 0x0001;
    if(v < 0.0f && v > -5.96046E-8f) return(short)0x8001;

    final int f = Float.floatToIntBits(v);

    return(short)((( f>>16 ) & 0x8000 ) | (((( f & 0x7f800000 ) - 0x38000000 )>>13 ) & 0x7c00 ) | (( f>>13 ) & 0x03ff ));
  }

  private List<Recognition> recognizeImageSsdMovileNetv2(Bitmap bitmap){
    Bitmap resizeBitmap = Bitmap.createScaledBitmap(bitmap, inputSizeSSD, inputSizeSSD, false);

    // Preprocess the image data from 0-255 int to normalized float based
    // on the provided parameters.
    resizeBitmap.getPixels(intValuesSSD, 0, resizeBitmap.getWidth(), 0, 0, resizeBitmap.getWidth(), resizeBitmap.getHeight());

    imgDataSSD.rewind();
    for (int i = 0; i < inputSizeSSD; ++i) {
      for (int j = 0; j < inputSizeSSD; ++j) {
        int pixelValue = intValuesSSD[i * inputSizeSSD + j];
        if (modelType== MyConstants.MODEL_TYPE.FLOAT32) {
          imgDataSSD.putFloat((((pixelValue >> 16) & 0xFF) - IMAGE_MEAN_SSD) / IMAGE_STD_SSD);
          imgDataSSD.putFloat((((pixelValue >> 8) & 0xFF) - IMAGE_MEAN_SSD) / IMAGE_STD_SSD);
          imgDataSSD.putFloat(((pixelValue & 0xFF) - IMAGE_MEAN_SSD) / IMAGE_STD_SSD);
        }else if(modelType== MyConstants.MODEL_TYPE.UINT8){
          // Quantized model
          imgDataSSD.put((byte) ((pixelValue >> 16) & 0xFF));
          imgDataSSD.put((byte) ((pixelValue >> 8) & 0xFF));
          imgDataSSD.put((byte) (pixelValue & 0xFF));
        }
      }
    }

    final long startTime = SystemClock.uptimeMillis();
    Object[] inputArray = {imgDataSSD};
    Map<Integer, Object> outputMap = new HashMap<>();
    outputMap.put(0, ouputSSDBoxes  );
    outputMap.put(1, ouputSSDClasses);
    outputMap.put(2, ouputSSDScores );

    tfLiteSSD.runForMultipleInputsOutputs(inputArray, outputMap);

    long lastProcessingTimeMs = SystemClock.uptimeMillis() - startTime;
    Log.d("============== Prediction time object detection ", ":" + lastProcessingTimeMs);

    // Show the best detections.
    // after scaling them back to the input size.
    final ArrayList<Recognition> recognitions = new ArrayList<>(NUM_DETECTIONS);
    for (int i = 0; i < NUM_DETECTIONS; ++i) {
      if(ouputSSDScores[0][i]<scoreThreshold){
        continue;
      }

      final RectF detection =
              new RectF( 1-ouputSSDBoxes[0][i][0],
                         ouputSSDBoxes[0][i][1],
                         1-ouputSSDBoxes[0][i][2],
                         ouputSSDBoxes[0][i][3]
                       );
      // SSD Mobilenet V1 Model assumes class 0 is background class
      // in label file and class labels start from 1 to number_of_classes+1,
      // while outputClasses correspond to class index from 0 to number_of_classes
      recognitions.add(
              new Recognition(
                      "" + i,
                      labelsSSD[Math.round(ouputSSDClasses[0][i])],
                      ouputSSDScores[0][i],
                      detection,
                      lastProcessingTimeMs));
    }

    return recognitions;

  }

  @Override
  public void enableStatLogging(final boolean logStats) {}

  @Override
  public String getStatString() {
    return "";
  }

  @Override
  public void close() {
    /** Closes the interpreter and model to release resources. */
    if (tfLiteSSD != null) {
      tfLiteSSD.close();
      tfLiteSSD = null;
    }
    if (gpuDelegateSSD != null) {
      gpuDelegateSSD.close();
      gpuDelegateSSD = null;
    }
  }

  public void setNumThreads(int num_threads) {
    if (tfLiteSSD != null) tfLiteSSD.setNumThreads(num_threads);
  }

  @Override
  public void setUseNNAPI(boolean isChecked) {
    if (tfLiteSSD != null) tfLiteSSD.setUseNNAPI(isChecked);
  }
}
