db.createUser({
    user: "dev",
    pwd: "test",
    roles: [{ role: "readWrite", db: "test" }]
});
