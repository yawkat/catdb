function hasPermission(user, permission, context) {
    if (user == "yawkat") {
        return true;
    }
    // allow query.* and rate
    if (/^query\..*$/.test(permission) || permission == "rate") {
        return true;
    }
    if (context.channel == "#thinkofcat") {
        // allow add to everyone in #thinkofcat
        if (permission == "add") {
            return true;
        }
    }
    return false;
}