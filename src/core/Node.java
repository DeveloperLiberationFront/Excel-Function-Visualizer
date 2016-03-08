package core;

public interface Node {
  void add(int ex, FormulaToken token);

  void setChildren();

  int increment();

  int getFrequency();

  String toString();
}