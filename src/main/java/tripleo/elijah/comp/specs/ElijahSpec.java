package tripleo.elijah.comp.specs;

import tripleo.wrap.File;
import java.io.*;

public record ElijahSpec(String f, File file, InputStream s, boolean do_out) {
}
