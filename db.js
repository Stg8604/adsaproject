const sqlite3 = require('sqlite3').verbose();

let db = new sqlite3.Database(':memory:');


db.serialize(function() {
  db.run("CREATE TABLE payments (id INT PRIMARY KEY, name TEXT, amount REAL, date TEXT)");


  var stmt = db.prepare("INSERT INTO payments VALUES (?, ?, ?, ?)");
  for (var i = 0; i < 10; i++) {
      stmt.run(i, "Payment " + i, i * 10, new Date().toISOString());
  }
  stmt.finalize();


  db.each("SELECT id, name, amount, date FROM payments", function(err, row) {
      console.log(row.id + ": " + row.name + ", " + row.amount + ", " + row.date);
  });
});


db.close();
