package tripleo.elijah.stages.deduce;


import tripleo.elijah.comp.ErrSink;
import tripleo.elijah.stages.logging.ElLog;

record DT_Env(ElLog LOG, ErrSink errSink, DeduceCentral central) {
}
