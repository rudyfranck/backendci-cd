package co.freea.tsn.Training.Core.Service;

import co.freea.tsn.Training.Core.Models.*;

import co.freea.tsn.Training.Core.Models.ResponseModel.DeepMapper;
import co.freea.tsn.Training.Core.Util.Logger;
import io.reactivex.Flowable;
import io.reactivex.schedulers.Schedulers;
import org.apache.commons.lang.StringUtils;
import org.davidmoten.rx.jdbc.Database;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;

import java.lang.reflect.Field;
import java.sql.Timestamp;
import java.util.*;
import java.util.stream.Collectors;

@Service
@Component
public class UniversalRepository<T extends CommonService> {

    @Autowired
    private Database database;

    @SafeVarargs
    public final Mono<Integer> insert(T... in) {

        int count = 0;

        for (T obj : in) {
            Field[] fields = obj.getClass().getDeclaredFields();

            List<String> names = new ArrayList<>();

            List<Object> params = Arrays
                    .stream(fields)
                    .filter(field -> {
                        field.setAccessible(true);
                        try {
                            return Optional.ofNullable(field.get(obj)).isPresent();
                        } catch (IllegalAccessException e) {
                            return false;
                        }
                    })
                    .map(field -> {
                        try {
                            names.add(field.getName());
                            return field.get(obj);
                        } catch (IllegalAccessException e) {
                            return null;
                        } finally {
                            field.setAccessible(false);
                        }
                    }).collect(Collectors.toList());

            Logger.debug(params);
            Logger.debug(names);

            String values = params.stream().map(o -> {
                if (o instanceof UUID) {
                    return "'" + o.toString() + "'";
                }
                if (o instanceof String) {
                    return "'" + o + "'";
                } else {
                    return String.valueOf(o);
                }
            }).collect(Collectors.joining(","));

            String req = "INSERT INTO " + obj.getTable() + "(" + String.join(",", names) + ") VALUES (" + values + ")";

            Logger.debug(req);

            count += this.database
                    .update(req)
                    .counts()
                    .subscribeOn(Schedulers.io())
                    .blockingFirst(0);
        }

        return Mono.just(count);
    }

    public Mono<T> findOne(UUID id, Class<T> current) throws Exception {
        Logger.debug("TemporaryRepository", "findOne");
        T instance = current.getDeclaredConstructor().newInstance();
        String query = "SELECT * FROM " + instance.getTable() + " WHERE id = '" + id + "'";
        Flowable<T> flow = this.database
                .select(query)
                .get(rs -> DeepMapper.<T>analyseForObject(rs, current, false).orElse(null))
                .subscribeOn(Schedulers.io());
        return Mono.from(flow);
    }

    public Mono<PageResponse<T>> findDeepOne(T instance, int page_number, int page_size) throws Exception {
        Logger.debug("TemporaryRepository", "findOne");

        List<String> builder = new ArrayList<>();

        for (Field field : instance.getClass().getDeclaredFields()) {
            if (!Optional.ofNullable(field).isPresent()) {
                continue;
            }
            boolean state = field.isAccessible();
            field.setAccessible(true);
            if (Optional.ofNullable(field.get(instance)).isPresent()
                    && !field.getName().equalsIgnoreCase("table")) {
                try {
                    if (field.get(instance) instanceof Timestamp ||
                            field.get(instance) instanceof Date) {
                        continue;
                    }
                    if (StringUtils.isNumeric(String.valueOf(field.get(instance)))) {
                        builder.add(field.getName() + "=" + field.get(instance) + "");
                    } else if (field.getName().contentEquals("status") && field.get(instance) instanceof Boolean) {
                        Logger.debug();
                    } else {
                        builder.add(field.getName() + " LIKE '%" + field.get(instance) + "%'");
                    }
                } catch (Exception e) {
                    Logger.debug(e.getMessage());
                } finally {
                    field.setAccessible(state);
                }
            }
        }

        String init = "SELECT * FROM "
                + instance.getTable()
                + " WHERE "
                + String.join(" OR ", builder);

        String query;

        if (builder.size() <= 0) {

            query = "SELECT * FROM "
                    + instance.getTable()
                    + " ORDER BY created_at DESC"
                    + " LIMIT " + page_size
                    + " OFFSET " + ((page_number == 0 ? 1 : page_number) - 1) * page_size;
        } else {
            query = init
                    + " ORDER BY created_at DESC"
                    + " LIMIT " + page_size
                    + " OFFSET " + ((page_number == 0 ? 1 : page_number) - 1) * page_size;
        }


        Logger.debug(query);

        Flowable<PageResponse<T>> flow = this.database
                .select(query)
                .get(rs -> {
                    long sizeTotal = this.database.select(init).get(set -> {
                        long count = 0;
                        do {
                            count++;
                        }
                        while (set.next());
                        return count;
                    }).blockingFirst(0L);
                    List<T> list = new ArrayList<>();
                    do {
                        T tmp = DeepMapper.<T>analyseForObject(rs, instance.getClass(), false).orElse(null);
                        list.add(tmp);
                        Logger.debug(tmp);
                    } while (rs.next());
                    return new PageResponse<>((long) Math.ceil((double) sizeTotal / (double) page_size), list);
                })
                .subscribeOn(Schedulers.io());
        return Mono.from(flow);
    }

    public Mono<PageResponse<T>> findAll(Class<T> current, int page_number, int page_size) throws Exception {
        Logger.debug("UniversalRepository", "findAll");
        T instance = current.getDeclaredConstructor().newInstance();
        String query = "SELECT * FROM " + instance.getTable();
        Flowable<PageResponse<T>> flux = this.database.select(query)
                .fetchSize(page_size)
                .get(rs -> {
                    long count = 0L;
                    List<T> list = new ArrayList<>();
                    do {
                        T tmp = DeepMapper.<T>analyseForObject(rs, instance.getClass(), false).orElse(null);
                        list.add(tmp);
                        Logger.debug(tmp);
                        count++;
                    } while (rs.next());
                    return new PageResponse<>((long) Math.ceil((double) count / (double) page_size), list);

                })
                .subscribeOn(Schedulers.io());
        return Mono.from(flux);
    }

    public Mono<List<FolderResult>> getTree() {
        String query = "SELECT * FROM folders WHERE parent IS NULL";
        Logger.debug(query);
        Flowable<List<FolderResult>> flux = this.database.select(query)
                .get(rs -> {
                    List<FolderResult> list = new ArrayList<>();
                    do {
                        FolderResult tmp = DeepMapper.<FolderResult>analyseForObject(rs, FolderResult.class, false).orElse(null);
                        if (!Optional.ofNullable(tmp).isPresent()) {
                            continue;
                        }
                        tmp.apply(Folder.class);
                        tmp.setChildren(construct_tree(tmp).toArray(new FolderResult[0]));
                        list.add(tmp);
                    } while (rs.next());
                    return list;
                })
                .subscribeOn(Schedulers.io());

        Logger.debug("\n\n\n\n\n\n\n\n\n");
        Logger.debug(flux);
        return Mono.from(flux);
    }


    public List<FolderResult> construct_tree(FolderResult result) {
        String query = "SELECT * FROM folders WHERE parent = '" + result.getId().toString() + "'";


        String query_op = "SELECT * FROM operations WHERE parent = '" + result.getId().toString() + "'";


        String query_techno = "SELECT * FROM technologies WHERE parent = '" + result.getId().toString() + "'";


        Logger.debug(query);


        List<FolderResult> outcome = new ArrayList<>();

        outcome.addAll(this.database
                .select(query)
                .get(rs -> {
                    Logger.debug("IN REQ FOLDER");

                    Logger.debug(query);
                    List<FolderResult> folderList = new ArrayList<>();
                    do {
                        FolderResult tmp = DeepMapper.<FolderResult>analyseForObject(rs, FolderResult.class, false).orElse(null);
                        if (!Optional.ofNullable(tmp).isPresent()) {
                            continue;
                        }
                        tmp.apply(Folder.class);
                        tmp.setChildren(construct_tree(tmp).toArray(new FolderResult[0]));
                        folderList.add(tmp);
                        Logger.debug(tmp);
                    } while (rs.next());
                    return folderList;
                }).blockingFirst(new ArrayList<>()));

        Logger.debug(query_techno);

        outcome.addAll(this.database
                .select(query_techno)
                .get(rs -> {
                    Logger.debug("IN REQ TEHNO");
                    List<FolderResult> technoList = new ArrayList<>();
                    do {
                        FolderResult tmp = DeepMapper.<FolderResult>analyseForObject(rs, FolderResult.class, false).orElse(null);
                        if (!Optional.ofNullable(tmp).isPresent()) {
                            continue;
                        }

                        String subQuery = "SELECT * FROM operations WHERE id_technology = '" + tmp.getId().toString() + "'";
                        List<FolderResult> subOperationList = this.database
                                .select(subQuery)
                                .get(res -> {
                                    List<FolderResult> operationList = new ArrayList<>();
                                    Logger.debug("IN REQ OP");
                                    do {
                                        FolderResult tmp2 = DeepMapper.<FolderResult>analyseForObject(res, FolderResult.class, false).orElse(null);
                                        if (!Optional.ofNullable(tmp2).isPresent()) {
                                            continue;
                                        }
                                        tmp2.apply(Operation.class);
                                        operationList.add(tmp2);
                                        Logger.debug(tmp2);
                                    } while (res.next());
                                    return operationList;
                                }).blockingFirst(new ArrayList<>());

                        tmp.apply(Technology.class);
                        tmp.setOperations(subOperationList.toArray(new FolderResult[0]));
                        technoList.add(tmp);
                        Logger.debug(tmp);
                    } while (rs.next());
                    return technoList;
                }).blockingFirst(new ArrayList<>()));


        Logger.debug(query_op);

        outcome.addAll(this.database.
                select(query_op)
                .get(rs -> {
                    List<FolderResult> operationList = new ArrayList<>();

                    Logger.debug("IN REQ OP");
                    do {
                        FolderResult tmp = DeepMapper.<FolderResult>analyseForObject(rs, FolderResult.class, false).orElse(null);
                        if (!Optional.ofNullable(tmp).isPresent()) {
                            continue;
                        }
                        tmp.apply(Operation.class);
                        operationList.add(tmp);
                        Logger.debug(tmp);
                    } while (rs.next());
                    return operationList;
                }).blockingFirst(new ArrayList<>()));

//        return this.database
//                .select(query)
//                .get(rs -> {
//                    Logger.debug("IN REQ FOLDER");
//
//                    Logger.debug(query);
//                    List<FolderResult> list = new ArrayList<>();
//                    do {
//                        FolderResult tmp = DeepMapper.<FolderResult>analyseForObject(rs, FolderResult.class, false).orElse(null);
//                        if (!Optional.ofNullable(tmp).isPresent()) {
//                            continue;
//                        }
//                        tmp.apply(Folder.class);
//                        tmp.setChildren(construct_tree(tmp).toArray(new FolderResult[0]));
//                        list.add(tmp);
//                        Logger.debug(tmp);
//                    } while (rs.next());
//                    return list;
//                })
//                .map(level1 -> {
//                    Logger.debug(query_techno);
//
//                    this.database
//                            .select(query_techno)
//                            .get(rs -> {
//                                Logger.debug("IN REQ TEHNO");
//
//                                do {
//                                    FolderResult tmp = DeepMapper.<FolderResult>analyseForObject(rs, FolderResult.class, false).orElse(null);
//                                    if (!Optional.ofNullable(tmp).isPresent()) {
//                                        continue;
//                                    }
//                                    tmp.apply(Technology.class);
//                                    tmp.setChildren(construct_tree(tmp).toArray(new FolderResult[0]));
//                                    level1.add(tmp);
//                                    Logger.debug(tmp);
//                                } while (rs.next());
//                                return level1;
//                            }).blockingFirst(new ArrayList<>());
//
//                    return level1;
//                })
//                .map(level1 -> {
//
//                    Logger.debug(query_op);
//
//                    this.database.
//                            select(query_op)
//                            .get(rs -> {
//
//                                Logger.debug("IN REQ OP");
//                                do {
//                                    FolderResult tmp = DeepMapper.<FolderResult>analyseForObject(rs, FolderResult.class, false).orElse(null);
//                                    if (!Optional.ofNullable(tmp).isPresent()) {
//                                        continue;
//                                    }
//                                    tmp.apply(Operation.class);
//                                    tmp.setChildren(construct_tree(tmp).toArray(new FolderResult[0]));
//                                    level1.add(tmp);
//                                    Logger.debug(tmp);
//                                } while (rs.next());
//                                return level1;
//                            }).blockingFirst(new ArrayList<>());
//                    return level1;
//                })
//                .blockingFirst(new ArrayList<>());
        return outcome;
    }

    public Mono<Integer> deleteById(UUID id, Class<T> current) throws Exception {
        T instance = current.getDeclaredConstructor().newInstance();
        String query = "DELETE FROM " + instance.getTable() + " WHERE id='" + id.toString() + "'";
        return Mono.from(this.database.update(query).counts());
    }
}
