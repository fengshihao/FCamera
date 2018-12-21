package fengshihao.com.fcameralib;

import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

import android.util.Log;
import android.util.Pair;

public class Camera2States {

  private static final String TAG = "Camera2States";

  enum State {
    IDLE,
    OPENING,
    OPEN,
    CLOSING,
    ERROR
  }


  private State mCurrentState = State.IDLE;

  private final static Set<Pair<State, State>> mAllow;
  static {
    Set<Pair<State, State>> set = new HashSet<>();

    set.add(new Pair<>(State.IDLE, State.OPENING));
    set.add(new Pair<>(State.OPENING, State.OPEN));
    set.add(new Pair<>(State.OPEN, State.CLOSING));
    set.add(new Pair<>(State.CLOSING, State.IDLE));
    set.add(new Pair<>(State.OPENING, State.ERROR));
    set.add(new Pair<>(State.OPEN, State.ERROR));
    set.add(new Pair<>(State.CLOSING, State.ERROR));
    mAllow = Collections.unmodifiableSet(set);
  }

  boolean canGotoState(State st) {
    Log.d(TAG, "canGotoState() current=" + mCurrentState + " st= " + st);
    return (mAllow.contains(new Pair<>(mCurrentState, st)));
  }

  boolean toState(State st) {
    Log.d(TAG, "toState() current=" + mCurrentState + " st= " + st);
    if (canGotoState(st)) {
      mCurrentState = st;
      Log.d(TAG, "toState: " + mCurrentState);
      return true;
    }
    return false;
  }

  State current() {
    return mCurrentState;
  }
}
