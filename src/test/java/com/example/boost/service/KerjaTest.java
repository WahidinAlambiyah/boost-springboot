package com.example.boost.service;

import java.io.*;
import java.util.*;

/**
 * Generic "Social Media App" simulator template.
 * Adjust commands + input/output according to the exact problem statement.
 */
public class KerjaTest {

    // static class Post {
    //     long id;
    //     int author;
    //     long time;
    //     int likes;

    //     Post(long id, int author, long time) {
    //         this.id = id;
    //         this.author = author;
    //         this.time = time;
    //         this.likes = 0;
    //     }
    // }

    // static class FeedNode {
    //     int author;
    //     int idx;      // index of post in author's list
    //     Post post;

    //     FeedNode(int author, int idx, Post post) {
    //         this.author = author;
    //         this.idx = idx;
    //         this.post = post;
    //     }
    // }

    // public static void main(String[] args) throws Exception {
    //     FastScanner fs = new FastScanner(System.in);
    //     String first = fs.next();
    //     if (first == null) return;

    //     // Common formats:
    //     // 1) Q then Q lines of commands
    //     // 2) Commands until EOF
    //     int Q;
    //     boolean firstIsNumber = isNumber(first);
    //     if (firstIsNumber) {
    //         Q = Integer.parseInt(first);
    //     } else {
    //         Q = -1; // unknown, read until EOF; first token is command
    //     }

    //     Map<Integer, Set<Integer>> follows = new HashMap<>();
    //     Map<Integer, List<Post>> postsByUser = new HashMap<>();
    //     Map<Long, Post> postById = new HashMap<>();

    //     StringBuilder out = new StringBuilder();

    //     int processed = 0;
    //     String cmd = firstIsNumber ? fs.next() : first;

    //     while (cmd != null && (Q < 0 || processed < Q)) {
    //         processed++;

    //         switch (cmd) {
    //             case "FOLLOW": {
    //                 int a = fs.nextInt();
    //                 int b = fs.nextInt();
    //                 follows.computeIfAbsent(a, k -> new HashSet<>()).add(b);
    //                 // Many problems require self-follow:
    //                 // follows.get(a).add(a);
    //                 break;
    //             }
    //             case "UNFOLLOW": {
    //                 int a = fs.nextInt();
    //                 int b = fs.nextInt();
    //                 Set<Integer> set = follows.get(a);
    //                 if (set != null) set.remove(b);
    //                 // Usually keep self-follow if required by statement
    //                 break;
    //             }
    //             case "POST": {
    //                 int u = fs.nextInt();
    //                 long postId = fs.nextLong();
    //                 long time = fs.nextLong(); // sometimes implicit = increasing counter
    //                 Post p = new Post(postId, u, time);
    //                 postsByUser.computeIfAbsent(u, k -> new ArrayList<>()).add(p);
    //                 postById.put(postId, p);
    //                 break;
    //             }
    //             case "LIKE": {
    //                 int u = fs.nextInt();      // sometimes user not needed
    //                 long postId = fs.nextLong();
    //                 Post p = postById.get(postId);
    //                 if (p != null) p.likes++;
    //                 break;
    //             }
    //             case "FEED": {
    //                 int u = fs.nextInt();
    //                 int k = fs.nextInt();

    //                 // Build follow set
    //                 Set<Integer> fset = follows.getOrDefault(u, Collections.emptySet());

    //                 // Some statements include user's own posts automatically:
    //                 // create a temp set that includes u
    //                 // (do this only if statement requires)
    //                 Set<Integer> all = new HashSet<>(fset);
    //                 all.add(u);

    //                 List<Long> feed = getLatestFeed(all, postsByUser, k);

    //                 // Output format depends on statement:
    //                 // Example: print postIds separated by space in one line
    //                 for (int i = 0; i < feed.size(); i++) {
    //                     if (i > 0) out.append(' ');
    //                     out.append(feed.get(i));
    //                 }
    //                 out.append('\n');
    //                 break;
    //             }

    //             default:
    //                 // If statement uses short commands like "P", "F", etc.,
    //                 // map them here.
    //                 // If unknown command => might mean format differs.
    //                 // You can throw to detect early during local run.
    //                 // throw new RuntimeException("Unknown command: " + cmd);
    //                 break;
    //         }

    //         cmd = fs.next();
    //     }

    //     System.out.print(out.toString());
    // }

    // static List<Long> getLatestFeed(Set<Integer> authors,
    //                                Map<Integer, List<Post>> postsByUser,
    //                                int k) {
    //     PriorityQueue<FeedNode> pq = new PriorityQueue<>((a, b) -> {
    //         // order by time desc, tie by postId desc (adjust if needed)
    //         if (a.post.time != b.post.time) return Long.compare(b.post.time, a.post.time);
    //         return Long.compare(b.post.id, a.post.id);
    //     });

    //     for (int author : authors) {
    //         List<Post> list = postsByUser.get(author);
    //         if (list == null || list.isEmpty()) continue;
    //         int idx = list.size() - 1;
    //         pq.add(new FeedNode(author, idx, list.get(idx)));
    //     }

    //     List<Long> res = new ArrayList<>();
    //     while (!pq.isEmpty() && res.size() < k) {
    //         FeedNode node = pq.poll();
    //         res.add(node.post.id);

    //         int nextIdx = node.idx - 1;
    //         if (nextIdx >= 0) {
    //             List<Post> list = postsByUser.get(node.author);
    //             pq.add(new FeedNode(node.author, nextIdx, list.get(nextIdx)));
    //         }
    //     }
    //     return res;
    // }

    // static boolean isNumber(String s) {
    //     for (int i = 0; i < s.length(); i++) if (!Character.isDigit(s.charAt(i))) return false;
    //     return !s.isEmpty();
    // }

    // // Fast scanner
    // static class FastScanner {
    //     private final InputStream in;
    //     private final byte[] buffer = new byte[1 << 16];
    //     private int ptr = 0, len = 0;

    //     FastScanner(InputStream is) { in = is; }

    //     private int read() throws IOException {
    //         if (ptr >= len) {
    //             len = in.read(buffer);
    //             ptr = 0;
    //             if (len <= 0) return -1;
    //         }
    //         return buffer[ptr++];
    //     }

    //     String next() throws IOException {
    //         StringBuilder sb = new StringBuilder();
    //         int c;
    //         while ((c = read()) != -1 && Character.isWhitespace(c)) {}
    //         if (c == -1) return null;
    //         do {
    //             sb.append((char) c);
    //             c = read();
    //         } while (c != -1 && !Character.isWhitespace(c));
    //         return sb.toString();
    //     }

    //     int nextInt() throws IOException { return Integer.parseInt(next()); }
    //     long nextLong() throws IOException { return Long.parseLong(next()); }
    // }

    /** ################## 
     *  ### Fibonacci ### 
     *  ##################*/
    // public static void main(String[] args) {
    //     long n = 7; // sample: cari F(10)
    //     long ans = fibonacci(n);
    //     System.out.println("F(" + n + ") = " + ans);
    // }

    // static long fibonacci(long n) {
    //     if (n <= 1) return 0;
    //     if (n == 2) return 1;

    //     long a = 0, b = 1;
    //     for (long i = 3; i <= n; i++) {
    //         long c = a + b;
    //         a = b;
    //         b = c;
    //     }
    //     return b;
    // }


    /** ################## 
     *  ### Arithmetic ### 
     *  ##################*/
    // public static void main(String[] args) {
    //     long a1 = 2;   // sample: 2, 5, 8, 11, ...
    //     long d  = 3;
    //     long n  = 6;   // cari suku ke-6 => 17
    //     long ans = arithmeticTerm(a1, d, n);
    //     System.out.println("a_" + n + " = " + ans);
    // }

    // static long arithmeticTerm(long a1, long d, long n) {
    //     return a1 + (n - 1) * d;
    // }

    /** ################## 
     *  ### deret ### 
     *  ##################*/
    // public static void main(String[] args) {
    //     long n = 10; // sample: suku ke-6 => 16 (1,2,4,7,11,16)
    //     long ans = mixedSequenceTerm(n);
    //     System.out.println("a_" + n + " = " + ans);
    // }

    // static long mixedSequenceTerm(long n) {
    //     return 1 + (n - 1) * n / 2;
    // }

    public static void main(String[] args) throws Exception {
        BufferedReader br = new BufferedReader(new InputStreamReader(System.in));
        String s = br.readLine().trim();
        System.out.print(Solve.decode(s));
    }

    static class Solve {
        static String decode(String s) {
            Deque<StringBuilder> strStack = new ArrayDeque<>();
            Deque<Integer> kStack = new ArrayDeque<>();

            StringBuilder cur = new StringBuilder();
            int i = 0;
            int n = s.length();

            while (i < n) {
                char c = s.charAt(i);

                if (Character.isDigit(c)) {
                    // parse full number K
                    int k = 0;
                    while (i < n && Character.isDigit(s.charAt(i))) {
                        k = k * 10 + (s.charAt(i) - '0');
                        i++;
                    }
                    // next char should be '(' based on valid input
                    if (i < n && s.charAt(i) == '(') {
                        strStack.push(cur);
                        kStack.push(k);
                        cur = new StringBuilder();
                        i++; // skip '('
                    }
                } else if (c == '(') {
                    // In case there's '(' without a number (shouldn't happen per statement, but safe)
                    strStack.push(cur);
                    kStack.push(1);
                    cur = new StringBuilder();
                    i++;
                } else if (c == ')') {
                    int k = kStack.pop();
                    StringBuilder prev = strStack.pop();

                    int repeat = (k + 1) / 2; // floor((k+1)/2)

                    // append cur repeated
                    // (output guaranteed <= 1e5 so looping is safe)
                    for (int r = 0; r < repeat; r++) {
                        prev.append(cur);
                    }
                    cur = prev;
                    i++;
                } else {
                    // lowercase letter
                    cur.append(c);
                    i++;
                }
            }

            return cur.toString();
        }
    }
}
