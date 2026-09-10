#!/usr/bin/env python3
"""
Repeatable, no-debugger check of <TAB> completion behaviour.

Drives the shell through a real pty and prints every byte it sends back
with control chars made visible:  \\a -> <BEL>  \\r -> <CR>  \\t -> <TAB>

    ./scripts/test-completion.py                 # prefix "xyz_"
    ./scripts/test-completion.py xyz_dog         # a different prefix
    ./scripts/test-completion.py --presses 3     # send TAB 3 times

"Multiple completions" stage wants, for  xyz_<TAB>:
    press 1 -> <BEL>, line unchanged, nothing listed
    press 2 -> lists: xyz_dog  xyz_owl  xyz_rat
"""
import argparse
import os
import pty
import select
import subprocess
import sys
import time

JAR = "/tmp/codecrafters-build-shell-java/codecrafters-shell.jar"
FAKE_BIN = "/tmp/bee"
FAKE_CMDS = ["xyz_dog", "xyz_owl", "xyz_rat"]


def make_fake_path():
    os.makedirs(FAKE_BIN, exist_ok=True)
    for name in FAKE_CMDS:
        p = os.path.join(FAKE_BIN, name)
        with open(p, "w") as f:
            f.write("#!/bin/sh\necho hi\n")
        os.chmod(p, 0o755)


def visible(b: bytes) -> str:
    out = []
    for ch in b:
        if ch == 0x07:
            out.append("<BEL>")
        elif ch == 0x0D:
            out.append("<CR>")
        elif ch == 0x0A:
            out.append("<LF>\n")
        elif ch == 0x09:
            out.append("<TAB>")
        elif ch == 0x1B:
            out.append("<ESC>")
        elif 0x20 <= ch < 0x7F:
            out.append(chr(ch))
        else:
            out.append(f"<{ch:02x}>")
    return "".join(out)


import re

# Minimal answers to the terminal-capability queries JLine fires at startup.
# A real terminal replies to these; a bare pty doesn't, so JLine stalls.
_QUERY_REPLIES = [
    (re.compile(rb"\x1b\[6n"), b"\x1b[1;1R"),            # cursor position report
    (re.compile(rb"\x1b\[>0?c"), b"\x1b[>0;10;0c"),      # secondary device attributes
    (re.compile(rb"\x1b\[\?0?c"), b"\x1b[?1;2c"),        # primary device attributes (?)
    (re.compile(rb"\x1b\[c"), b"\x1b[?1;2c"),            # primary device attributes
    (re.compile(rb"\x1b\[\?u"), b"\x1b[?0u"),            # kitty keyboard flags
    (re.compile(rb"\x1b\[(\?\d+)\$p"), None),            # DECRQM -> \e[<n>;2$y (handled below)
    (re.compile(rb"\x1bP\+q[0-9A-Fa-f]+\x1b\\"), b""),   # XTGETTCAP -> ignore
]


def answer_queries(fd, buf):
    for pat, reply in _QUERY_REPLIES:
        for m in pat.finditer(buf):
            if reply is None:  # DECRQM
                os.write(fd, b"\x1b[" + m.group(1).encode() if False else b"\x1b[" + m.group(1) + b";2$y")
            elif reply:
                os.write(fd, reply)


def drain(fd, label, timeout=1.5):
    buf = b""
    deadline = time.time() + timeout
    while time.time() < deadline:
        r, _, _ = select.select([fd], [], [], 0.2)
        if r:
            try:
                chunk = os.read(fd, 4096)
            except OSError:
                break
            if not chunk:
                break
            buf += chunk
            answer_queries(fd, chunk)
            deadline = time.time() + 0.4  # keep reading while data flows
    print(f"--- {label} ---")
    print(visible(buf) if buf else "(nothing)")
    print()
    return buf


def main():
    ap = argparse.ArgumentParser()
    ap.add_argument("word", nargs="?", default="xyz_")
    ap.add_argument("--presses", type=int, default=2)
    args = ap.parse_args()

    make_fake_path()
    env = dict(os.environ)
    env["PATH"] = f"{FAKE_BIN}:{env['PATH']}"

    pid, fd = pty.fork()
    if pid == 0:  # child
        os.execvpe(
            "java",
            ["java", "--enable-native-access=ALL-UNNAMED", "--enable-preview", "-jar", JAR],
            env,
        )
        os._exit(127)

    try:
        drain(fd, "startup / prompt")
        os.write(fd, args.word.encode())
        drain(fd, f"typed {args.word!r}")
        for i in range(1, args.presses + 1):
            os.write(fd, b"\t")
            drain(fd, f"press {i} (TAB)")
        os.write(fd, b"\x03")  # ctrl-c
        time.sleep(0.2)
    finally:
        try:
            os.close(fd)
        except OSError:
            pass
        try:
            os.waitpid(pid, 0)
        except ChildProcessError:
            pass


if __name__ == "__main__":
    if not os.path.exists(JAR):
        sys.exit(f"jar not built: {JAR}\n  run: mvn -q -B package -Ddir=/tmp/codecrafters-build-shell-java")
    main()
